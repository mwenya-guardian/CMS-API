package com.cms.repository;

import com.cms.model.PublicationReaction;
import com.cms.model.ReactionBaseDocument;
import com.cms.model.ReactionBaseDocument.ReactionType;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PublicationReactionRepository extends MongoRepository<PublicationReaction, String> {
    List<PublicationReaction> findByPublicationId(String publicationId);
    List<PublicationReaction> findByPublicationIdAndType(String publicationId, ReactionBaseDocument.ReactionType type);
    List<PublicationReaction> findByUserId(String userId);
    List<PublicationReaction> findByUserIdAndPublicationIdAndType(String userId, String publicationId, ReactionBaseDocument.ReactionType type);
    List<PublicationReaction> findByTypeNotAndUserIdAndPublicationId(ReactionBaseDocument.ReactionType type, String userId, String publicationId);
    List<PublicationReaction> findByUserIdAndType(String userId, ReactionBaseDocument.ReactionType type);
    long countByUserIdAndPublicationIdAndType(String userId, String publicationId, ReactionBaseDocument.ReactionType type);
    long countByPublicationIdAndType(String publicationId, ReactionType type);
    void deleteByPublicationIdAndUserIdAndType(String publicationId, String userId, ReactionType type);
}
