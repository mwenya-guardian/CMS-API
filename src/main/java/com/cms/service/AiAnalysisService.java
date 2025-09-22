package com.cms.service;

import com.cms.client.AiClient;
import com.cms.model.AnalysisJob;
import com.cms.dto.request.CommentRequest;
import com.cms.model.CommentAnalysis;
import com.cms.repository.AnalysisJobRepository;
import com.cms.repository.CommentAnalysisRepository;
import com.cms.dto.request.BulkAnalysisRequest;
import com.cms.dto.response.BulkAnalysisResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AiAnalysisService {

    private final AiClient aiClient;
    private final CommentAnalysisRepository analysisRepo;
    private final AnalysisJobRepository jobRepo;
    private final TrendService trendService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // config
    private final int CHUNK_SIZE = 40;    // tune for token limits
    private final int PARALLELISM = 3;    // if you do async concurrency

    public BulkAnalysisResponse submitBulk(@NotNull  BulkAnalysisRequest request, List<CommentRequest> comments) {
        // comments: load from your comment repository by entityType/entityId and since...
        int total = comments.size();
//        AnalysisJob job = AnalysisJob.builder()
//                .entityType(request.getEntityType())
//                .entityId(request.getEntityId())
//                .submittedAt(Instant.now())
//                .status(AnalysisJob.AnalysisStatus.PENDING)
//                .totalComments(total)
//                .processedComments(0)
//                .meta(Map.of("chunkSize", CHUNK_SIZE))
//                .build();

        AnalysisJob job = new AnalysisJob();
            job.setEntityType(request.getEntityType());
            job.setEntityId(request.getEntityId());
            job.setSubmittedAt(Instant.now());
            job.setStatus(AnalysisJob.AnalysisStatus.PENDING);
            job.setTotalComments(total);
            job.setProcessedComments(0);
            job.setMeta(Map.of("chunkSize", CHUNK_SIZE));


        job = jobRepo.save(job);

        // chunking
        List<List<CommentRequest>> chunks = chunk(comments, CHUNK_SIZE);

        // schedule asynchronous processing of each chunk
        AtomicInteger submitted = new AtomicInteger();
        for (List<CommentRequest> chunk : chunks) {
            submitted.incrementAndGet();
            processChunkAsync(job.getId(), request.getEntityType(), request.getEntityId(), chunk, request.getModes());
        }

        job.setStatus(AnalysisJob.AnalysisStatus.RUNNING);
        jobRepo.save(job);
        return new BulkAnalysisResponse(job.getId(), submitted.get());
    }

    private List<List<CommentRequest>> chunk(List<CommentRequest> comments, int size) {
        List<List<CommentRequest>> chunks = new ArrayList<>();
        for (int i = 0; i < comments.size(); i += size) {
            chunks.add(comments.subList(i, Math.min(comments.size(), i + size)));
        }
        return chunks;
    }

    @Async("aiExecutor") // configure ThreadPoolTaskExecutor bean named aiExecutor
    public void processChunkAsync(String jobId, ReactionService.ReactionCategory entityType, String entityId, List<CommentRequest> chunk, String[] modes) {
        try {
            // Build inputs for AiClient
            List<CommentRequest> inputs = chunk.stream().map(c ->
                    new CommentRequest(c.id(), c.content(), c.createdAt())
            ).collect(Collectors.toList());

            // optional context, e.g. post title
            String context = "Entity: " + entityType + " id:" + entityId;

            // call LLM
            String raw = aiClient.analyzeChunk(inputs, context);

            // parse JSON array result from raw. The provider may wrap result - extract model text portion if needed.
            String jsonArray = extractJsonArrayFromModelResponse(raw);

            List<ChunkResponse> results = objectMapper.readValue(jsonArray, new TypeReference<List<ChunkResponse>>() {});
            // persist results per comment
            List<CommentAnalysis> toSave = new ArrayList<>();
            for (ChunkResponse r : results) {
                CommentAnalysis ca = CommentAnalysis.builder()
                        .commentId(r.getId())
                        .entityType(entityType)
                        .entityId(entityId)
                        .sentiment(r.getSentiment())
                        .sentimentScore(r.getSentimentScore())
                        .tone(r.getTone())
                        .toneConfidence(r.getToneConfidence())
                        .moderationFlagged(r.getModeration() != null && r.getModeration().isFlagged())
                        .moderationCategories(r.getModeration() != null ? r.getModeration().getCategories() : null)
                        .moderationConfidence(r.getModeration() != null ? r.getModeration().getConfidence() : null)
                        .modelName("gemini")
                        .modelVersion("1.5 flash")
                        .analyzedAt(Instant.now())
                        .rawResponse(objectMapper.writeValueAsString(r))
                        .build();
                toSave.add(ca);
            }
            analysisRepo.saveAll(toSave);

            // update processed count in job
            AnalysisJob job = jobRepo.findById(jobId).orElseThrow();
            job.setProcessedComments(job.getProcessedComments() + chunk.size());
            if (job.getProcessedComments() >= job.getTotalComments()) {
                job.setStatus(AnalysisJob.AnalysisStatus.SUCCESS);
                job.setCompletedAt(Instant.now());
            }
            jobRepo.save(job);

            // update trend / aggregates and run alerts
            trendService.recomputeAggregatesAndMaybeAlert(entityType, entityId);

        } catch (Exception ex) {
            AnalysisJob job = jobRepo.findById(jobId).orElse(null);
            if (job != null) {
                job.setStatus(AnalysisJob.AnalysisStatus.FAILED);
                job.setFailureReason(ex.getMessage());
                job.setCompletedAt(Instant.now());
                jobRepo.save(job);
            }
            // log exception - for brevity print stack (replace with proper logger)
            ex.printStackTrace();
        }
    }

    private String extractJsonArrayFromModelResponse(String raw) {
        // Best-effort: attempt to find first '[' ... ']' that looks like a JSON array.
        // For robustness, you may use a regex or provider-specific extraction.
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        // if not found, return raw (let the parser fail and the chunk be retried)
        return raw;
    }


    @Getter
    @Setter
    public static class ChunkResponse {
        private String id;
        private String sentiment;
        private Double sentimentScore;
        private String tone;
        private Double toneConfidence;
        private Moderation moderation;

        @Setter
        @Getter
        public static class Moderation {
            private boolean flagged;
            private List<String> categories;
            private Double confidence;
        }
    }
}
