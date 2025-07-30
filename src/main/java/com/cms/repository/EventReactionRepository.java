package com.cms.repository;

import com.cms.model.EventReaction;
import com.cms.model.PublicationReaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface EventReactionRepository extends MongoRepository<EventReaction, String> {

    // find all reactions for a specific event
    List<EventReaction> findByEventId(String eventId);
    List<EventReaction> findByUserId(String userId);
    long countByEventIdAndType(String eventId, String type);

}
