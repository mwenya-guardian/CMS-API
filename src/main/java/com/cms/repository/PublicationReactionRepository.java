package com.cms.repository;

import com.cms.model.PublicationReaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PublicationReactionRepository extends MongoRepository<PublicationReaction, String> {
    List<PublicationReaction> findByPublicationId(String publicationId);
    List<PublicationReaction> findByUserId(String userId);
    long countByPublicationIdAndType(String publicationId, String type);
}
