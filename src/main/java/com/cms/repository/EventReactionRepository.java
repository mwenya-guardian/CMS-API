package com.cms.repository;

import com.cms.model.EventReaction;
import com.cms.model.ReactionBaseDocument;
import com.cms.model.ReactionBaseDocument.ReactionType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.time.Instant;
import java.util.List;

public interface EventReactionRepository extends MongoRepository<EventReaction, String> {

    // find all reactions for a specific event
    List<EventReaction> findByEventId(String eventId);
    List<EventReaction> findByUserId(String userId);
    List<EventReaction> findByUserIdAndEventIdAndType(String userId, String eventId, ReactionBaseDocument.ReactionType type);
    List<EventReaction> findByTypeNotAndUserIdAndEventId(ReactionBaseDocument.ReactionType type, String userId, String eventId);
    List<EventReaction> findByEventIdAndType(String eventId, ReactionBaseDocument.ReactionType type);
    Page<EventReaction> findByEventIdAndTypeAndAnalysedFalse(String eventId, ReactionBaseDocument.ReactionType type, Pageable pageable);
    Page<EventReaction> findByEventIdAndType(String eventId, ReactionBaseDocument.ReactionType type, Pageable pageable);
    List<EventReaction> findByUserIdAndType(String userId, ReactionBaseDocument.ReactionType type);
    long countByUserIdAndEventIdAndType(String userId, String eventId, ReactionBaseDocument.ReactionType type);
    long countByEventIdAndType(String eventId, ReactionBaseDocument.ReactionType type);
    void deleteByEventIdAndUserIdAndType(String eventId, String userId, ReactionType type);
    // Custom queries that updates the analysed field
    @Query(value = "{ 'eventId': ?0, 'type': ?1, 'analysed': false }", count = true)
    long countByEventIdAndTypeAndAnalysedFalse(String eventId, ReactionBaseDocument.ReactionType type);
    
    // Analysis support methods
    long countByType(ReactionBaseDocument.ReactionType type);
    List<EventReaction> findByEventIdAndTypeAndCreatedAtBetween(String eventId, ReactionBaseDocument.ReactionType type, Instant startDate, Instant endDate);
    List<EventReaction> findByTypeAndCreatedAtBetween(ReactionBaseDocument.ReactionType type, Instant startDate, Instant endDate);
}
