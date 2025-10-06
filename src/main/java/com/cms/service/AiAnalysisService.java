package com.cms.service;

import com.cms.client.AiClient;
import com.cms.dto.request.CommentRequest;
import com.cms.dto.request.BulkAnalysisRequest;
import com.cms.dto.response.BulkAnalysisResponse;
import com.cms.model.AnalysisJob;
import com.cms.model.CommentAnalysis;
import com.cms.model.ReactionBaseDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cms.repository.AnalysisJobRepository;
import com.cms.repository.CommentAnalysisRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * AiAnalysisService
 *
 * - Submit bulk analysis (creates AnalysisJob with chunk metadata)
 * - Process chunks asynchronously
 * - Persist CommentAnalysis documents
 * - Atomically update job progress using MongoTemplate ($inc on meta.completedChunks / meta.failedChunks and processedComments)
 * - When all chunks accounted for, finalize job status SUCCESS / FAILED
 *
 * Adapt field names if your domain objects differ.
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
    // private final ReactionTrackedModelService reactionTrackedModelService;
    private final MongoTemplate mongoTemplate;

    // config
    private final int CHUNK_SIZE = 40;    // tune for token limits
    private final Logger logger = LoggerFactory.getLogger(AiAnalysisService.class);
    private final Logger errorLogger = LoggerFactory.getLogger("error");

    // --- PUBLIC API -----------------------------------------------------

    /**
     * Submit bulk analysis job: create job record, chunk comments and kick off async processing.
     *
     * @param request request describing the job (entity type/id etc.)
     * @param comments comments to analyze (must already be fetched by caller)
     * @return BulkAnalysisResponse containing jobId and submitted chunk count
     */
    public BulkAnalysisResponse submitBulk(@javax.validation.constraints.NotNull BulkAnalysisRequest request,
                                           List<CommentRequest> comments) {
        int totalComments = comments == null ? 0 : comments.size();
        List<List<CommentRequest>> chunks = chunk(comments == null ? Collections.emptyList() : comments, CHUNK_SIZE);
        int totalChunks = chunks.size();

        // init meta with chunk tracking
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

        // context (optional)
        String context = getContext(request.getEntityType(), request.getEntityId());

        AtomicInteger submitted = new AtomicInteger(0);
        for (List<CommentRequest> chunk : chunks) {
//            submitted.incrementAndGet();
            processChunkAsync(job.getId(), request.getEntityType(), request.getEntityId(), chunk, context, submitted, totalChunks);
        }

        // mark running and return
        job.setStatus(AnalysisJob.AnalysisStatus.RUNNING);
        jobRepo.save(job);

        return new BulkAnalysisResponse(job.getId(), submitted.get());
    }

    /**
     * Submit bulk and wait for completion (sync). Polls job status until completion or timeout.
     */
    public String submitBulkAndWait(@javax.validation.constraints.NotNull BulkAnalysisRequest request,
                                    List<CommentRequest> comments) {
        BulkAnalysisResponse resp = submitBulk(request, comments);
        waitForJobCompletion(resp.jobId());
        return resp.jobId();
    }

    // --- HELPERS -------------------------------------------------------

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
            if (job == null) {
                break;
            }
            if (job.getStatus() == AnalysisJob.AnalysisStatus.SUCCESS ||
                    job.getStatus() == AnalysisJob.AnalysisStatus.FAILED) {
                break;
            }
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            waited += intervalMs;
        }
    }

    // --- ASYNC CHUNK PROCESSING ----------------------------------------

    @Async("aiExecutor")
    public void processChunkAsync(String jobId,
                                  ReactionService.ReactionCategory entityType,
                                  String entityId,
                                  List<CommentRequest> chunk,
                                  String context, AtomicInteger count, int totalChucks) {
        try {
            // Prepare inputs expected by your AiClient
            List<CommentRequest> inputs = chunk.stream()
                    .map(c -> new CommentRequest(c.id(), c.content(), c.createdAt()))
                    .collect(Collectors.toList());

            String rawModelResponse = aiClient.analyzeChunk(inputs, context);

            // Extract JSON array text from model response and parse
            String jsonArray = extractJsonArrayFromModelResponse(rawModelResponse);
            List<ChunkResponse> parsed = objectMapper.readValue(jsonArray, new TypeReference<List<ChunkResponse>>() {});

            // persist analyses
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
            commentAnalysisRepo.saveAll(toSave);

            // Mark this chunk completed (increment completedChunks by 1)
//            finalizeChunk(jobId, 1, false, null);

            // update analysed flag on comment/reaction objects (your method)
            for (CommentRequest c : chunk) {
                reactionService.updateAnalysedByTargetIdAndType(c.id(), ReactionBaseDocument.ReactionType.COMMENT, true, entityType);
            }

            // recompute aggregates & maybe alert
            trendService.recomputeAggregatesAndMaybeAlert(entityType, entityId);
            
            atomicIncProcessedComments(jobId, count, totalChucks, chunk.size());


        } catch (Exception ex) {
            errorLogger.error("Error processing chunk: " + ex.getMessage());
            ex.printStackTrace();

            // increment failedChunks by 1 and optionally persist failure reason
            // finalizeChunk(jobId, 0, true, ex.getMessage());
        }
    }

    // --- ATOMIC UPDATES (MongoTemplate) -------------------------------

    @Transactional
    private synchronized void atomicIncProcessedComments(String jobId, AtomicInteger chuckCount, int totalChucks, int commentCount) {
        if (commentCount <= 0) return;

        Query q = Query.query(Criteria.where("_id").is(jobId));

        chuckCount.incrementAndGet();
        AnalysisJob analysisJob = jobRepo.findById(jobId).orElseThrow();
        if(chuckCount.intValue() == totalChucks){
//            analysisJob.setStatus(AnalysisJob.AnalysisStatus.SUCCESS);
            mongoTemplate.updateFirst(q, new Update().set("status", AnalysisJob.AnalysisStatus.SUCCESS), AnalysisJob.class);
        } else if(analysisJob.getProcessedComments() < chuckCount.intValue()){
//            analysisJob.setStatus(AnalysisJob.AnalysisStatus.FAILED);
            mongoTemplate.updateFirst(q, new Update().set("status", AnalysisJob.AnalysisStatus.FAILED), AnalysisJob.class);
        }
//        jobRepo.save(analysisJob);
        logger.info("Processed comments: {} of {}", chuckCount.intValue(), totalChucks);

        Update u = new Update().inc("processedComments", commentCount);
        mongoTemplate.updateFirst(q, u, AnalysisJob.class);
    }

    /**
     * Atomically finalize a chunk by incrementing completedChunks or failedChunks and, if all chunks done,
     * setting the overall job status (SUCCESS if failedChunks == 0 else FAILED).
     *
     * @param jobId job id
     * @param completedChunkIncrement increment for completedChunks (usually 1 on success, 0 on failure)
     * @param failed whether this chunk failed
     * @param failureReason optional failure reason to append
     */
    private void finalizeChunk(String jobId, int completedChunkIncrement, boolean failed, String failureReason) {
        // Build atomic update for chunk counters
        Update update = new Update();
        if (completedChunkIncrement != 0) update.inc("meta.completedChunks", completedChunkIncrement);
        if (failed) update.inc("meta.failedChunks", 1);
        // ensure job status is at least RUNNING
        update.setOnInsert("status", AnalysisJob.AnalysisStatus.RUNNING);

        Query q = Query.query(Criteria.where("_id").is(jobId));
        // perform atomic increment
        mongoTemplate.findAndModify(q, update, FindAndModifyOptions.options().returnNew(true), AnalysisJob.class);

        // fetch current job and meta to evaluate completion
        AnalysisJob job = jobRepo.findById(jobId).orElse(null);
        if (job == null) return;

        Map<String, Object> meta = job.getMeta() == null ? new HashMap<>() : new HashMap<>(job.getMeta());
        int totalChunks = ((Number) meta.getOrDefault("totalChunks", 0)).intValue();
        int completedChunks = ((Number) meta.getOrDefault("completedChunks", 0)).intValue();
        int failedChunks = ((Number) meta.getOrDefault("failedChunks", 0)).intValue();

        // If failure reason provided, append to job.failureReason
        if (failureReason != null && !failureReason.isBlank()) {
            String prev = job.getFailureReason() == null ? "" : job.getFailureReason();
            job.setFailureReason((prev.isBlank() ? "" : prev + " | ") + failureReason);
        }

        // When all chunks accounted for, finalize
        if (totalChunks > 0 && (completedChunks + failedChunks) >= totalChunks) {
            if (failedChunks > 0) {
                job.setStatus(AnalysisJob.AnalysisStatus.FAILED);
                if (job.getFailureReason() == null) {
                    job.setFailureReason("One or more chunks failed");
                }
            } else {
                job.setStatus(AnalysisJob.AnalysisStatus.SUCCESS);
            }
            job.setCompletedAt(Instant.now());
            jobRepo.save(job); // persist final status & failure reason
        } else {
            // ensure job is at least RUNNING
            if (job.getStatus() == AnalysisJob.AnalysisStatus.PENDING) {
                job.setStatus(AnalysisJob.AnalysisStatus.RUNNING);
                jobRepo.save(job);
            }
        }
    }

    // --- UTIL: Extract JSON array from model response ------------------

    /**
     * Best-effort extraction: looks for the first '[' ... ']' substring in the model's response.
     * If not found, returns the input string (parsing will likely fail and be retried).
     */
    private String extractJsonArrayFromModelResponse(String raw) {
        if (raw == null) return "[]";
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }

    // --- Response DTO parsed from model --------------------------------

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
