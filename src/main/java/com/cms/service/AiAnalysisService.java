package com.cms.service;

import com.cms.client.AiClient;
import com.cms.model.AnalysisJob;
import com.cms.dto.request.CommentRequest;
import com.cms.model.CommentAnalysis;
import com.cms.model.ReactionBaseDocument;
import com.cms.model.ReactionTrackedModel;
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
    private final ReactionService reactionService;
    private final PostService postService;
    private final PublicationService publicationService;
    private final EventService eventService;
    private final QuoteService quoteService;
    private final ReactionTrackedModelService reactionTrackedModelService;
    // config
    private final int CHUNK_SIZE = 40;    // tune for token limits
    private final int PARALLELISM = 3; // if you do async concurrency

    //Get context
    public String getContext(ReactionService.ReactionCategory entityType, String entityId) {
        return switch (entityType) {
            case POST -> postService.getById(entityId).get().getCaption();
            case PUBLICATION -> publicationService.getPublicationById(entityId).get().getContent();
            case EVENT -> eventService.getEventById(entityId).get().getDescription();
            case QUOTE -> quoteService.getQuoteById(entityId).get().getText();
        };
    }

    public BulkAnalysisResponse submitBulk(@NotNull  BulkAnalysisRequest request, List<CommentRequest> comments) {
        // comments: load from your comment repository by entityType/entityId and since...
        int total = comments.size();
        AnalysisJob job = AnalysisJob.builder()
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .submittedAt(Instant.now())
                .status(AnalysisJob.AnalysisStatus.PENDING)
                .totalComments(total)
                .processedComments(0)
                .meta(Map.of("chunkSize", CHUNK_SIZE))
                .build();


        job = jobRepo.save(job);

        // chunking
        List<List<CommentRequest>> chunks = chunk(comments, CHUNK_SIZE);
        String context = getContext(request.getEntityType(), request.getEntityId());

        // schedule asynchronous processing of each chunk
        AtomicInteger submitted = new AtomicInteger();
        for (List<CommentRequest> chunk : chunks) {
            submitted.incrementAndGet();
            processChunkAsync(job.getId(), request.getEntityType(), request.getEntityId(), chunk, context);
        }

        job.setStatus(AnalysisJob.AnalysisStatus.RUNNING);
        jobRepo.save(job);
        return new BulkAnalysisResponse(job.getId(), submitted.get());
    }
    
    /**
     * Submit bulk analysis and wait for completion
     * @param request The bulk analysis request
     * @param comments The comments to analyze
     * @return The analysis job ID
     */
    public String submitBulkAndWait(@NotNull BulkAnalysisRequest request, List<CommentRequest> comments) {
        BulkAnalysisResponse response = submitBulk(request, comments);
        String jobId = response.jobId();
        
        // Wait for job completion
        waitForJobCompletion(jobId);
        
        return jobId;
    }
    
    /**
     * Wait for a job to complete (with timeout)
     * @param jobId The job ID to wait for
     */
    private void waitForJobCompletion(String jobId) {
        int maxWaitTime = 300; // 5 minutes timeout
        int waitInterval = 2; // Check every 2 seconds
        int waited = 0;
        
        while (waited < maxWaitTime) {
            AnalysisJob job = jobRepo.findById(jobId).orElse(null);
            if (job == null) {
                System.err.println("Job not found: " + jobId);
                break;
            }
            
            if (job.getStatus() == AnalysisJob.AnalysisStatus.SUCCESS || 
                job.getStatus() == AnalysisJob.AnalysisStatus.FAILED) {
                System.out.println("Job " + jobId + " completed with status: " + job.getStatus());
                break;
            }
            
            try {
                Thread.sleep(waitInterval * 1000);
                waited += waitInterval;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        if (waited >= maxWaitTime) {
            System.err.println("Job " + jobId + " timed out after " + maxWaitTime + " seconds");
        }
    }

    private List<List<CommentRequest>> chunk(List<CommentRequest> comments, int size) {
        List<List<CommentRequest>> chunks = new ArrayList<>();
        for (int i = 0; i < comments.size(); i += size) {
            chunks.add(comments.subList(i, Math.min(comments.size(), i + size)));
        }
        return chunks;
    }

    @Async("aiExecutor") // configure ThreadPoolTaskExecutor bean named aiExecutor
    public void processChunkAsync(String jobId, ReactionService.ReactionCategory entityType, String entityId, List<CommentRequest> chunk, String context) {
        try {
            // Build inputs for AiClient
            List<CommentRequest> inputs = chunk.stream().map(c ->
            new CommentRequest(c.id(), c.content(), c.createdAt())
            ).collect(Collectors.toList());
            
            
            // optional context, e.g. post title
            // String context = "Entity: " + entityType + " id:" + entityId;
            
            // call LLM
            String raw = aiClient.analyzeChunk(inputs, context);

            // parse JSON array result from raw. The provider may wrap result - extract model text portion if needed.
            String jsonArray = extractJsonArrayFromModelResponse(raw);
            System.out.println(jsonArray);

            List<ChunkResponse> results = objectMapper.readValue(jsonArray, new TypeReference<List<ChunkResponse>>() {});
            // persist results per comment
            List<CommentAnalysis> toSave = new ArrayList<>();
            for (ChunkResponse r : results) {
                CommentAnalysis ca = CommentAnalysis.builder()
                        .commentId(r.getCommentId() != null ? r.getCommentId() : r.getId())
                        .entityType(entityType)
                        .entityId(entityId)
                        .sentiment(r.getSentiment())
                        .sentimentScore(r.getSentimentScore())
//                        .tone(r.getTone())
//                        .toneConfidence(r.getToneConfidence())
                        .moderationFlagged(r.getModeration() != null && r.getModeration().isFlagged())
                        .moderationCategories(r.getModeration() != null ? r.getModeration().getCategories() : null)
                        .moderationConfidence(r.getModeration() != null ? r.getModeration().getConfidence() : null)
                        .modelName(aiClient.getModelName())
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
                
                // Note: Model marking as analyzed is now handled by the scheduler
                // to ensure it only happens after ALL pages are processed
            }
            jobRepo.save(job);
            // update analysed field for all comments in the chunk
            for (CommentRequest c : chunk) {
                reactionService.updateAnalysedByTargetIdAndType(c.id(), ReactionBaseDocument.ReactionType.COMMENT, true, entityType);
            }

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
    
    /**
     * Convert ReactionService.ReactionCategory to ReactionTrackedModel.ModelType
     */
    private ReactionTrackedModel.ModelType convertToModelType(ReactionService.ReactionCategory category) {
        return switch (category) {
            case POST -> ReactionTrackedModel.ModelType.POST;
            case PUBLICATION -> ReactionTrackedModel.ModelType.PUBLICATION;
            case EVENT -> ReactionTrackedModel.ModelType.EVENT;
            case QUOTE -> ReactionTrackedModel.ModelType.QUOTE;
        };
    }


    @Getter
    @Setter
    public static class ChunkResponse {
        private String id;
        private String commentId;  // Add this field to handle the JSON response
        private String sentiment;
        private Double sentimentScore;
        // private String tone;
        // private Double toneConfidence;
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
