package com.cms.repository;

import com.cms.model.CommentAnalysis;
import com.cms.service.ReactionService;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CommentAnalysisRepository extends MongoRepository<CommentAnalysis, String> {
    List<CommentAnalysis> findByEntityTypeAndEntityId(ReactionService.ReactionCategory entityType, String entityId);
    List<CommentAnalysis> findByEntityTypeAndEntityIdAndAnalyzedAtBetween(ReactionService.ReactionCategory entityType, String entityId, java.time.Instant start, java.time.Instant end);
    long countByEntityTypeAndEntityIdAndSentiment(ReactionService.ReactionCategory entityType, String entityId, String sentiment);
}
