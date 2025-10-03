package com.cms.repository;

import com.cms.model.ReactionTrackedModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionTrackedModelRepository extends MongoRepository<ReactionTrackedModel, String> {
    
    Optional<ReactionTrackedModel> findByModelIdAndModelType(String modelId, ReactionTrackedModel.ModelType modelType);
    
    List<ReactionTrackedModel> findByModelType(ReactionTrackedModel.ModelType modelType);
    
    List<ReactionTrackedModel> findByCommentsAnalyzed(Boolean commentsAnalyzed);
    
    List<ReactionTrackedModel> findByModelTypeAndCommentsAnalyzed(ReactionTrackedModel.ModelType modelType, Boolean commentsAnalyzed);
    
    boolean existsByModelIdAndModelType(String modelId, ReactionTrackedModel.ModelType modelType);
}
