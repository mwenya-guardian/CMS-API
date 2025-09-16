package com.cms.repository;

import com.cms.model.EventReaction;
import com.cms.model.ReactionBaseDocument;
import com.cms.model.ReactionBaseDocument.ReactionType;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface EventReactionRepository extends MongoRepository<EventReaction, String> {

    // find all reactions for a specific event
    List<EventReaction> findByEventId(String eventId);
    List<EventReaction> findByUserId(String userId);
    List<EventReaction> findByUserIdAndEventIdAndType(String userId, String eventId, ReactionBaseDocument.ReactionType type);
    List<EventReaction> findByTypeNotAndUserIdAndEventId(ReactionBaseDocument.ReactionType type, String userId, String eventId);
    List<EventReaction> findByEventIdAndType(String eventId, ReactionBaseDocument.ReactionType type);
    List<EventReaction> findByUserIdAndType(String userId, ReactionBaseDocument.ReactionType type);
    long countByUserIdAndEventIdAndType(String userId, String eventId, ReactionBaseDocument.ReactionType type);
    long countByEventIdAndType(String eventId, ReactionBaseDocument.ReactionType type);
    void deleteByEventIdAndUserIdAndType(String eventId, String userId, ReactionType type);
}
