package com.cms.repository;

import com.cms.model.AnalysisAlert;
import com.cms.service.ReactionService;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AnalysisAlertRepository extends MongoRepository<AnalysisAlert, String> {
    List<AnalysisAlert> findByEntityTypeAndEntityId(ReactionService.ReactionCategory entityType, String entityId);
}
