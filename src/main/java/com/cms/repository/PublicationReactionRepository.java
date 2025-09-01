package com.cms.repository;

import com.cms.model.EventReaction;
import com.cms.model.PublicationReaction;
import com.cms.model.ReactionBaseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PublicationReactionRepository extends MongoRepository<PublicationReaction, String> {
    List<PublicationReaction> findByPublicationId(String publicationId);
    List<PublicationReaction> findByUserId(String userId);
    List<PublicationReaction> findByUserIdAndPublicationIdAndType(String userId, String publicationId, ReactionBaseDocument.ReactionType type);
    long countByUserIdAndPublicationIdAndType(String userId, String publicationId, ReactionBaseDocument.ReactionType type);
    long countByPublicationIdAndType(String publicationId, String type);
}
