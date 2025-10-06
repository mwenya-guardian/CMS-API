package com.cms.service;

import com.cms.client.AiClient;
import com.cms.dto.request.CommentRequest;
import com.cms.dto.request.BulkAnalysisRequest;
import com.cms.dto.response.BulkAnalysisResponse;
import com.cms.model.AnalysisJob;
import com.cms.model.CommentAnalysis;
import com.cms.model.ReactionBaseDocument;
import com.cms.repository.AnalysisJobRepository;
import com.cms.repository.CommentAnalysisRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Revised AiAnalysisService - uses atomic DB updates to track chunk progress and finalize jobs.
 */
@Service
@AllArgsConstructor
public class AiAnalysisService {

    private final AiClient aiClient;
    private final CommentAnalysisRepository commentAnalysisRepo;
    private final AnalysisJobRepository jobRepo;
    private final TrendService trendService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReactionService reactionService;
    private final PostService postService;
    private final PublicationService publicationService;
    private final EventService eventService;
    private final QuoteService quoteService;
    private final MongoTemplate mongoTemplate;

    private final int CHUNK_SIZE = 40; // adjust for token limits
    private final Logger logger = LoggerFactory.getLogger(AiAnalysisService.class);
    private final Logger errorLogger = LoggerFactory.getLogger("error");

    // ---------------------
    // Public API
    // ---------------------

    public BulkAnalysisResponse submitBulk(@javax.validation.constraints.NotNull BulkAnalysisRequest request,
                                           List<CommentRequest> comments) {

        int totalComments = comments == null ? 0 : comments.size();
        List<List<CommentRequest>> chunks = chunk(comments == null ? Collections.emptyList() : comments, CHUNK_SIZE);
        int totalChunks = chunks.size();

        Map<String, Object> meta = new HashMap<>();
        meta.put("chunkSize", CHUNK_SIZE);
        meta.put("totalChunks", totalChunks);
        meta.put("completedChunks", 0);
        meta.put("failedChunks", 0);

        AnalysisJob job = AnalysisJob.builder()
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .submittedAt(Instant.now())
                .status(AnalysisJob.AnalysisStatus.PENDING)
                .totalComments(totalComments)
                .processedComments(0)
                .meta(meta)
                .build();

        job = jobRepo.save(job);

        // optional context for LLM
        String context = getContext(request.getEntityType(), request.getEntityId());

        // submit async chunk tasks (we pass chunk directly; DB tracks progress)
        // mark job running
        Query q = Query.query(Criteria.where("_id").is(job.getId()));
        mongoTemplate.updateFirst(q, new Update().set("status", AnalysisJob.AnalysisStatus.RUNNING), AnalysisJob.class);
        for (List<CommentRequest> chunk : chunks) {
            processChunkAsync(job.getId(), request.getEntityType(), request.getEntityId(), chunk, context);
        }


        // Return jobId and number of chunks submitted
        return new BulkAnalysisResponse(job.getId(), totalChunks);
    }

    public String submitBulkAndWait(@javax.validation.constraints.NotNull BulkAnalysisRequest request,
                                    List<CommentRequest> comments) {
        BulkAnalysisResponse resp = submitBulk(request, comments);
        waitForJobCompletion(resp.jobId());
        return resp.jobId();
    }

    // ---------------------
    // Helpers
    // ---------------------

    private String getContext(ReactionService.ReactionCategory entityType, String entityId) {
        return switch (entityType) {
            case POST -> postService.getById(entityId).orElseThrow().getCaption();
            case PUBLICATION -> publicationService.getPublicationById(entityId).orElseThrow().getContent();
            case EVENT -> eventService.getEventById(entityId).orElseThrow().getDescription();
            case QUOTE -> quoteService.getQuoteById(entityId).orElseThrow().getText();
        };
    }

    private List<List<CommentRequest>> chunk(List<CommentRequest> comments, int size) {
        List<List<CommentRequest>> chunks = new ArrayList<>();
        for (int i = 0; i < comments.size(); i += size) {
            chunks.add(comments.subList(i, Math.min(comments.size(), i + size)));
        }
        return chunks;
    }

    private void waitForJobCompletion(String jobId) {
        int maxWaitSeconds = 300; // 5 minutes
        int intervalMs = 2000;
        int waited = 0;
        while (waited < maxWaitSeconds * 1000) {
            AnalysisJob job = jobRepo.findById(jobId).orElse(null);
            if (job == null) break;
            if (job.getStatus() == AnalysisJob.AnalysisStatus.SUCCESS || job.getStatus() == AnalysisJob.AnalysisStatus.FAILED) break;
            try { Thread.sleep(intervalMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            waited += intervalMs;
        }
    }

    // ---------------------
    // Async chunk processing
    // ---------------------

    @Async("aiExecutor")
    public void processChunkAsync(String jobId,
                                  ReactionService.ReactionCategory entityType,
                                  String entityId,
                                  List<CommentRequest> chunk,
                                  String context) {
        int commentCount = (chunk == null) ? 0 : chunk.size();

        try {
            // Prepare inputs for LLM
            List<CommentRequest> inputs = chunk.stream()
                    .map(c -> new CommentRequest(c.id(), c.content(), c.createdAt()))
                    .collect(Collectors.toList());

            // Call AiClient
            String rawModelResponse = aiClient.analyzeChunk(inputs, context);

            // Extract JSON from model response and parse
            String jsonArray = extractJsonArrayFromModelResponse(rawModelResponse);
            List<ChunkResponse> parsed = objectMapper.readValue(jsonArray, new TypeReference<List<ChunkResponse>>() {});

            // Persist analyses
            List<CommentAnalysis> toSave = new ArrayList<>();
            for (ChunkResponse r : parsed) {
                CommentAnalysis ca = CommentAnalysis.builder()
                        .commentId(r.getCommentId() != null ? r.getCommentId() : r.getId())
                        .entityType(entityType)
                        .entityId(entityId)
                        .sentiment(r.getSentiment())
                        .sentimentScore(r.getSentimentScore())
                        .moderationFlagged(r.getModeration() != null && r.getModeration().isFlagged())
                        .moderationCategories(r.getModeration() != null ? r.getModeration().getCategories() : null)
                        .moderationConfidence(r.getModeration() != null ? r.getModeration().getConfidence() : null)
                        .modelName(aiClient.getModelName())
                        .analyzedAt(Instant.now())
                        .rawResponse(objectMapper.writeValueAsString(r))
                        .build();
                toSave.add(ca);
            }
            if (!toSave.isEmpty()) commentAnalysisRepo.saveAll(toSave);

            // update analyzed flag on original comment/reaction objects
            for (CommentRequest c : chunk) {
                reactionService.updateAnalysedByTargetIdAndType(c.id(), ReactionBaseDocument.ReactionType.COMMENT, true, entityType);
            }

            // recompute trends/alerts
            trendService.recomputeAggregatesAndMaybeAlert(entityType, entityId);

            // Atomically mark chunk success (increment completedChunks and processedComments)
            finalizeChunk(jobId, /*completedChunkIncrement*/ 1, /*failed*/ false, commentCount, null);

        } catch (Exception ex) {
            errorLogger.error("Error processing chunk for job {}: {}", jobId, ex.getMessage(), ex);

            // Atomically mark chunk failed (increment failedChunks)
            try {
                finalizeChunk(jobId, /*completedChunkIncrement*/ 0, /*failed*/ true, 0, ex.getMessage());
            } catch (Exception e2) {
                errorLogger.error("Error finalizing failed chunk for job {}: {}", jobId, e2.getMessage(), e2);
            }
        }
    }

    // ---------------------
    // Atomic finalizeChunk
    // ---------------------

    /**
     * Atomically update chunk counters and processedComments.
     * If after the update all chunks are accounted for, finalize job status.
     *
     * @param jobId job id
     * @param completedChunkIncrement usually 1 when chunk succeeded, 0 otherwise
     * @param failed whether chunk failed
     * @param commentCount number of comments processed in this chunk (affects processedComments counter)
     * @param failureReason optional failure reason to append
     */
    private synchronized void finalizeChunk(String jobId, int completedChunkIncrement, boolean failed, int commentCount, String failureReason) {
        Query q = Query.query(Criteria.where("_id").is(jobId));

        // Build atomic update
        Update update = new Update();
        if (completedChunkIncrement != 0) update.inc("meta.completedChunks", completedChunkIncrement);
        if (failed) update.inc("meta.failedChunks", 1);
        if (commentCount > 0) update.inc("processedComments", commentCount);
        // Ensure we set job to RUNNING at least
        update.setOnInsert("status", AnalysisJob.AnalysisStatus.RUNNING);

        // Atomically apply increments and return the NEW document
        FindAndModifyOptions opts = FindAndModifyOptions.options().returnNew(true).upsert(false);
        AnalysisJob updated = mongoTemplate.findAndModify(q, update, opts, AnalysisJob.class);

        if (updated == null) {
            // Job may have been deleted; nothing to do
            logger.warn("finalizeChunk: AnalysisJob not found for id={}", jobId);
            return;
        }

        // Append failure reason if provided (non-atomic append separate update)
        if (failureReason != null && !failureReason.isBlank()) {
            // try to safely append (second update)
            Update append = new Update().set("failureReason",
                    (updated.getFailureReason() == null || updated.getFailureReason().isBlank())
                            ? failureReason
                            : (updated.getFailureReason() + " | " + failureReason));
            mongoTemplate.updateFirst(q, append, AnalysisJob.class);
            // reload updated to get latest meta/failureReason
            updated = jobRepo.findById(jobId).orElse(updated);
        }

        // Read meta counters defensively
        Map<String, Object> meta = updated.getMeta() == null ? Collections.emptyMap() : updated.getMeta();
        int totalChunks = ((Number) meta.getOrDefault("totalChunks", 0)).intValue();
        int completedChunks = ((Number) meta.getOrDefault("completedChunks", 0)).intValue();
        int failedChunks = ((Number) meta.getOrDefault("failedChunks", 0)).intValue();

        logger.info("Job {} progress: completedChunks={}, failedChunks={}, totalChunks={}, processedComments={}",
                jobId, completedChunks, failedChunks, totalChunks, updated.getProcessedComments());

        // If all chunks accounted for, finalize job status
        if (totalChunks > 0 && (completedChunks + failedChunks) >= totalChunks) {
            AnalysisJob.AnalysisStatus finalStatus = (failedChunks > 0) ? AnalysisJob.AnalysisStatus.FAILED : AnalysisJob.AnalysisStatus.SUCCESS;
            Update finish = new Update()
                    .set("status", finalStatus)
                    .set("completedAt", Instant.now());
            mongoTemplate.updateFirst(q, finish, AnalysisJob.class);
            logger.info("Job {} finalized as {}", jobId, finalStatus);
        } else {
            // Ensure status is at least RUNNING
            if (updated.getStatus() == AnalysisJob.AnalysisStatus.PENDING) {
                mongoTemplate.updateFirst(q, new Update().set("status", AnalysisJob.AnalysisStatus.RUNNING), AnalysisJob.class);
            }
        }
    }

    // ---------------------
    // Utilities
    // ---------------------

    private String extractJsonArrayFromModelResponse(String raw) {
        if (raw == null) return "[]";
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }

    // ---------------------
    // ChunkResponse DTO
    // ---------------------

    @Getter
    @Setter
    public static class ChunkResponse {
        private String id;
        private String commentId;
        private String sentiment;
        private Double sentimentScore;
        private Moderation moderation;

        @Getter
        @Setter
        public static class Moderation {
            private boolean flagged;
            private List<String> categories;
            private Double confidence;
        }
    }
}
