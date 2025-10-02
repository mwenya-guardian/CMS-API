package com.cms.repository;

import com.cms.model.CommentAnalysis;
import com.cms.service.ReactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.time.Instant;
import java.util.List;

public interface CommentAnalysisRepository extends MongoRepository<CommentAnalysis, String> {
    List<CommentAnalysis> findByEntityTypeAndEntityId(ReactionService.ReactionCategory entityType, String entityId);
    Page<CommentAnalysis> findByEntityTypeAndEntityId(ReactionService.ReactionCategory entityType, String entityId, Pageable pageable);
    List<CommentAnalysis> findByEntityTypeAndEntityIdAndAnalyzedAtBetween(ReactionService.ReactionCategory entityType, String entityId, Instant start, Instant end);
    long countByEntityTypeAndEntityIdAndSentiment(ReactionService.ReactionCategory entityType, String entityId, String sentiment);
    
    // New methods for analysis functionality
    long countByModerationFlagged(boolean flagged);
    long countBySentiment(String sentiment);
    Page<CommentAnalysis> findByModerationFlagged(boolean flagged, Pageable pageable);
    List<CommentAnalysis> findByAnalyzedAtBetween(Instant start, Instant end);
    
    @Query("{'moderationFlagged': ?0, $and: [" +
           "{'$or': [{'entityType': {$exists: false}}, {'entityType': ?1}]}, " +
           "{'$or': [{'sentiment': {$exists: false}}, {'sentiment': ?2}]}" +
           "]}")
    Page<CommentAnalysis> findByModerationFlaggedAndFilters(boolean flagged, ReactionService.ReactionCategory entityType, String sentiment, Pageable pageable);
}
