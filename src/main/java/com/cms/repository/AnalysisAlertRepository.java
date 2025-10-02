package com.cms.repository;

import com.cms.model.AnalysisAlert;
import com.cms.service.ReactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.Instant;
import java.util.List;

public interface AnalysisAlertRepository extends MongoRepository<AnalysisAlert, String> {
    List<AnalysisAlert> findByEntityTypeAndEntityId(ReactionService.ReactionCategory entityType, String entityId);
    Page<AnalysisAlert> findByEntityTypeAndEntityId(ReactionService.ReactionCategory entityType, String entityId, Pageable pageable);
    List<AnalysisAlert> findByCreatedAtAfterOrderByCreatedAtDesc(Instant after);
}
