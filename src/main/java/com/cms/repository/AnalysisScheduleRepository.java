package com.cms.repository;

import com.cms.model.AnalysisSchedule;
import com.cms.model.ReactionTrackedModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisScheduleRepository extends MongoRepository<AnalysisSchedule, String> {
    
    List<AnalysisSchedule> findByEnabledTrue();
    
    List<AnalysisSchedule> findByModelType(ReactionTrackedModel.ModelType modelType);
    
    List<AnalysisSchedule> findByModelTypeAndEnabledTrue(ReactionTrackedModel.ModelType modelType);
}
