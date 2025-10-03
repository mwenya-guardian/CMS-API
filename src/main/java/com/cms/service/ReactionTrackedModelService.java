package com.cms.service;

import com.cms.model.ReactionTrackedModel;
import com.cms.repository.ReactionTrackedModelRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ReactionTrackedModelService {
    
    private final ReactionTrackedModelRepository reactionTrackedModelRepository;
    
    /**
     * Track a model that has reactions attached to it
     * @param modelId The ID of the model being tracked
     * @param modelType The type of the model (POST, PUBLICATION, EVENT, QUOTE)
     * @return The created ReactionTrackedModel
     */
    public ReactionTrackedModel trackModel(String modelId, ReactionTrackedModel.ModelType modelType) {
        // Check if already tracked
        if (reactionTrackedModelRepository.existsByModelIdAndModelType(modelId, modelType)) {
            return reactionTrackedModelRepository.findByModelIdAndModelType(modelId, modelType)
                    .orElseThrow(() -> new RuntimeException("Model tracking record not found"));
        }
        
        ReactionTrackedModel trackedModel = new ReactionTrackedModel(modelId, modelType);
        return reactionTrackedModelRepository.save(trackedModel);
    }
    
    /**
     * Update the comments analyzed status for a tracked model
     * @param modelId The ID of the model
     * @param modelType The type of the model
     * @param commentsAnalyzed The new status
     * @return The updated ReactionTrackedModel
     */
    public ReactionTrackedModel updateCommentsAnalyzedStatus(String modelId, ReactionTrackedModel.ModelType modelType, Boolean commentsAnalyzed) {
        ReactionTrackedModel trackedModel = reactionTrackedModelRepository.findByModelIdAndModelType(modelId, modelType)
                .orElseThrow(() -> new RuntimeException("Model tracking record not found"));
        
        trackedModel.setCommentsAnalyzed(commentsAnalyzed);
        return reactionTrackedModelRepository.save(trackedModel);
    }
    
    /**
     * Get a tracked model by model ID and type
     * @param modelId The ID of the model
     * @param modelType The type of the model
     * @return Optional containing the ReactionTrackedModel if found
     */
    public Optional<ReactionTrackedModel> getTrackedModel(String modelId, ReactionTrackedModel.ModelType modelType) {
        return reactionTrackedModelRepository.findByModelIdAndModelType(modelId, modelType);
    }
    
    /**
     * Get all tracked models by type
     * @param modelType The type of the model
     * @return List of ReactionTrackedModel
     */
    public List<ReactionTrackedModel> getTrackedModelsByType(ReactionTrackedModel.ModelType modelType) {
        return reactionTrackedModelRepository.findByModelType(modelType);
    }
    
    /**
     * Get all tracked models by comments analyzed status
     * @param commentsAnalyzed The status to filter by
     * @return List of ReactionTrackedModel
     */
    public List<ReactionTrackedModel> getTrackedModelsByCommentsAnalyzed(Boolean commentsAnalyzed) {
        return reactionTrackedModelRepository.findByCommentsAnalyzed(commentsAnalyzed);
    }
    
    /**
     * Get all tracked models by type and comments analyzed status
     * @param modelType The type of the model
     * @param commentsAnalyzed The status to filter by
     * @return List of ReactionTrackedModel
     */
    public List<ReactionTrackedModel> getTrackedModelsByTypeAndCommentsAnalyzed(ReactionTrackedModel.ModelType modelType, Boolean commentsAnalyzed) {
        return reactionTrackedModelRepository.findByModelTypeAndCommentsAnalyzed(modelType, commentsAnalyzed);
    }
    
    /**
     * Check if a model is being tracked
     * @param modelId The ID of the model
     * @param modelType The type of the model
     * @return true if the model is being tracked
     */
    public boolean isModelTracked(String modelId, ReactionTrackedModel.ModelType modelType) {
        return reactionTrackedModelRepository.existsByModelIdAndModelType(modelId, modelType);
    }
    
    /**
     * Remove tracking for a model
     * @param modelId The ID of the model
     * @param modelType The type of the model
     */
    public void removeTracking(String modelId, ReactionTrackedModel.ModelType modelType) {
        Optional<ReactionTrackedModel> trackedModel = reactionTrackedModelRepository.findByModelIdAndModelType(modelId, modelType);
        trackedModel.ifPresent(reactionTrackedModelRepository::delete);
    }
}
