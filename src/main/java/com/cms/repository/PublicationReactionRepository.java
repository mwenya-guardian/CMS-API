package com.cms.repository;

import com.cms.model.PublicationReaction;
import com.cms.model.ReactionBaseDocument;
import com.cms.model.ReactionBaseDocument.ReactionType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface PublicationReactionRepository extends MongoRepository<PublicationReaction, String> {
    List<PublicationReaction> findByPublicationId(String publicationId);
    List<PublicationReaction> findByPublicationIdAndType(String publicationId, ReactionBaseDocument.ReactionType type);
    Page<PublicationReaction> findByPublicationIdAndTypeAndAnalysedFalse(String publicationId, ReactionBaseDocument.ReactionType type, Pageable pageable);
    Page<PublicationReaction> findByPublicationIdAndType(String publicationId, ReactionBaseDocument.ReactionType type, Pageable pageable);
    List<PublicationReaction> findByUserId(String userId);
    List<PublicationReaction> findByUserIdAndPublicationIdAndType(String userId, String publicationId, ReactionBaseDocument.ReactionType type);
    List<PublicationReaction> findByTypeNotAndUserIdAndPublicationId(ReactionBaseDocument.ReactionType type, String userId, String publicationId);
    List<PublicationReaction> findByUserIdAndType(String userId, ReactionBaseDocument.ReactionType type);
    long countByUserIdAndPublicationIdAndType(String userId, String publicationId, ReactionBaseDocument.ReactionType type);
    long countByPublicationIdAndType(String publicationId, ReactionType type);
    void deleteByPublicationIdAndUserIdAndType(String publicationId, String userId, ReactionType type);
    // Custom queries that updates the analysed field
    @Query(value = "{ 'publicationId': ?0, 'type': ?1, 'analysed': false }", count = true)
    long countByPublicationIdAndTypeAndAnalysedFalse(String publicationId, ReactionBaseDocument.ReactionType type);
    
    // Analysis support methods
    long countByType(ReactionBaseDocument.ReactionType type);
    List<PublicationReaction> findByPublicationIdAndTypeAndCreatedAtBetween(String publicationId, ReactionBaseDocument.ReactionType type, Instant startDate, Instant endDate);
    List<PublicationReaction> findByTypeAndCreatedAtBetween(ReactionBaseDocument.ReactionType type, Instant startDate, Instant endDate);
}
