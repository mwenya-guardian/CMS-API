package com.cms.service;

import com.cms.dto.request.BulkAnalysisRequest;
import com.cms.dto.request.CommentRequest;
import com.cms.dto.response.PageResponse;
import com.cms.model.AnalysisSchedule;
import com.cms.model.ReactionTrackedModel;
import com.cms.repository.AnalysisScheduleRepository;
import com.cms.service.ReactionService.ReactionCategory;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@AllArgsConstructor
public class AnalysisSchedulerService {

    private final AnalysisScheduleRepository scheduleRepo;
    private final TaskScheduler taskScheduler;
    private final ReactionTrackedModelService reactionTrackedModelService;
    private final ReactionService reactionService;
    private final AiAnalysisService aiAnalysisService;

    // track scheduled futures to allow cancellation/reschedule
    private final Map<String, ScheduledFuture<?>> jobs = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // schedule all enabled schedules on startup
        List<AnalysisSchedule> schedules = scheduleRepo.findAll();
        schedules.forEach(this::schedule);
    }

    public void schedule(AnalysisSchedule schedule) {
        if (!schedule.isEnabled()) return;

        CronTrigger trigger = new CronTrigger(schedule.getCronExpression(), java.util.TimeZone.getTimeZone(schedule.getZoneId()));
        ScheduledFuture<?> future = taskScheduler.schedule(() -> runSchedule(schedule.getId()), trigger);
        jobs.put(schedule.getId(), future);
    }

    public void cancel(String scheduleId) {
        ScheduledFuture<?> future = jobs.remove(scheduleId);
        if (future != null) future.cancel(false);
    }

    public void reschedule(AnalysisSchedule schedule) {
        cancel(schedule.getId());
        schedule(schedule);
    }

    // core runner
    public void runSchedule(String scheduleId) {
        AnalysisSchedule schedule = scheduleRepo.findById(scheduleId).orElse(null);
        if (schedule == null || !schedule.isEnabled()) return;
        
        try {
            System.out.println("Starting analysis schedule: " + schedule.getTitle() + " for model type: " + schedule.getModelType());
            
            // Get all tracked models of the specified type
            List<ReactionTrackedModel> trackedModels = reactionTrackedModelService
                    .getTrackedModelsByType(schedule.getModelType());
            
            if (trackedModels.isEmpty()) {
                System.out.println("No untracked models found for type: " + schedule.getModelType());
                return;
            }
            
            int totalModelsProcessed = 0;
            
            // Process each tracked model
            for (ReactionTrackedModel trackedModel : trackedModels) {
                try {
                    System.out.println("Processing model: " + trackedModel.getModelId() + " of type: " + trackedModel.getModelType());
                    
                    // Convert ModelType to ReactionCategory
                    ReactionCategory reactionCategory = convertToReactionCategory(trackedModel.getModelType());
                    
                    // Process comments page by page
                    boolean hasComments = processModelCommentsPageByPage(trackedModel.getModelId(), reactionCategory);
                    
                    if (!hasComments) {
                        System.out.println("No comments found for model: " + trackedModel.getModelId());
                    }
                    // If hasComments is true, the model will be marked as analyzed when the last page completes
                    // (handled in AiAnalysisService.processChunkAsync)
                    
                    totalModelsProcessed++;
                    System.out.println("Completed analysis for model: " + trackedModel.getModelId());
                    
                } catch (Exception ex) {
                    System.err.println("Error processing model " + trackedModel.getModelId() + ": " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            
            // Update schedule metadata
            schedule.setLastRunAt(Instant.now());
            scheduleRepo.save(schedule);
            
            System.out.println("Analysis schedule completed. Processed " + totalModelsProcessed + " models");
            
        } catch (Exception ex) {
            System.err.println("Error in analysis schedule " + scheduleId + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Process comments for a model page by page, submitting each page to submitBulk
     * @param modelId The model ID to process
     * @param category The reaction category
     * @return true if any comments were found and processed, false otherwise
     */
    private boolean processModelCommentsPageByPage(String modelId, ReactionCategory category) {
        int page = 1;
        int pageSize = 120; // Process in batches
        boolean hasComments = false;
        List<String> jobIds = new java.util.ArrayList<>();
        
        // First pass: collect all pages and submit jobs
        while (true) {
            PageResponse<CommentRequest> pageResponse = reactionService.getCommentForAnalysis(modelId, category, pageSize, page);
            
            if (pageResponse.getData().isEmpty()) {
                break; // No more comments
            }
            
            hasComments = true;
            
            // Submit this page for analysis
            BulkAnalysisRequest bulkRequest = new BulkAnalysisRequest();
            bulkRequest.setEntityType(category);
            bulkRequest.setEntityId(modelId);
            
            System.out.println("Submitting page " + page + " for model " + modelId + " with " + pageResponse.getData().size() + " comments");
            String jobId = aiAnalysisService.submitBulkAndWait(bulkRequest, pageResponse.getData());
            jobIds.add(jobId);
            
            // Check if we've reached the last page
            if (page >= pageResponse.getPagination().getTotalPages()) {
                break;
            }
            
            page++;
        }
        
        // Mark model as analyzed after all pages are processed
        if (hasComments) {
            System.out.println("Marked model " + modelId + " as analyzed after processing " + jobIds.size() + " pages");
        }
        
        return hasComments;
    }
    
    /**
     * Convert ReactionCategory to ReactionTrackedModel.ModelType
     */
    private ReactionTrackedModel.ModelType convertToModelType(ReactionCategory category) {
        return switch (category) {
            case POST -> ReactionTrackedModel.ModelType.POST;
            case PUBLICATION -> ReactionTrackedModel.ModelType.PUBLICATION;
            case EVENT -> ReactionTrackedModel.ModelType.EVENT;
            case QUOTE -> ReactionTrackedModel.ModelType.QUOTE;
        };
    }
    
    /**
     * Convert ReactionTrackedModel.ModelType to ReactionService.ReactionCategory
     */
    private ReactionCategory convertToReactionCategory(ReactionTrackedModel.ModelType modelType) {
        return switch (modelType) {
            case POST -> ReactionCategory.POST;
            case PUBLICATION -> ReactionCategory.PUBLICATION;
            case EVENT -> ReactionCategory.EVENT;
            case QUOTE -> ReactionCategory.QUOTE;
        };
    }
    
    // CRUD operations for schedules
    public List<AnalysisSchedule> getAllSchedules() {
        return scheduleRepo.findAll();
    }
    
    public AnalysisSchedule getScheduleById(String id) {
        return scheduleRepo.findById(id).orElse(null);
    }
    
    public AnalysisSchedule createSchedule(AnalysisSchedule schedule) {
        AnalysisSchedule savedSchedule = scheduleRepo.save(schedule);
        if (savedSchedule.isEnabled()) {
            schedule(savedSchedule);
        }
        return savedSchedule;
    }
    
    public AnalysisSchedule updateSchedule(String id, AnalysisSchedule schedule) {
        AnalysisSchedule existingSchedule = scheduleRepo.findById(id).orElse(null);
        if (existingSchedule == null) {
            return null;
        }
        
        // Update fields
        existingSchedule.setTitle(schedule.getTitle());
        existingSchedule.setCronExpression(schedule.getCronExpression());
        existingSchedule.setZoneId(schedule.getZoneId());
        existingSchedule.setModelType(schedule.getModelType());
        existingSchedule.setDescription(schedule.getDescription());
        
        AnalysisSchedule savedSchedule = scheduleRepo.save(existingSchedule);
        
        // Reschedule if enabled
        if (savedSchedule.isEnabled()) {
            reschedule(savedSchedule);
        } else {
            cancel(savedSchedule.getId());
        }
        
        return savedSchedule;
    }
    
    public boolean deleteSchedule(String id) {
        AnalysisSchedule schedule = scheduleRepo.findById(id).orElse(null);
        if (schedule == null) {
            return false;
        }
        
        cancel(id);
        scheduleRepo.deleteById(id);
        return true;
    }
    
    public boolean enableSchedule(String id) {
        AnalysisSchedule schedule = scheduleRepo.findById(id).orElse(null);
        if (schedule == null) {
            return false;
        }
        
        schedule.setEnabled(true);
        scheduleRepo.save(schedule);
        schedule(schedule);
        return true;
    }
    
    public boolean disableSchedule(String id) {
        AnalysisSchedule schedule = scheduleRepo.findById(id).orElse(null);
        if (schedule == null) {
            return false;
        }
        
        schedule.setEnabled(false);
        scheduleRepo.save(schedule);
        cancel(id);
        return true;
    }
    
    public void runScheduleNow(String id) {
        runSchedule(id);
    }
    
    public List<AnalysisSchedule> getSchedulesByModelType(ReactionTrackedModel.ModelType modelType) {
        return scheduleRepo.findByModelTypeAndEnabledTrue(modelType);
    }
}
