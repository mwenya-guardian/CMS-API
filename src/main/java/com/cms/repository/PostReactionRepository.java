package com.cms.repository;

import com.cms.model.PostReaction;
import com.cms.model.ReactionBaseDocument;
import com.cms.model.ReactionBaseDocument.ReactionType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface PostReactionRepository extends MongoRepository<PostReaction, String> {
    // find all reactions for a specific post
    List<PostReaction> findByPostId(String postId);
    List<PostReaction> findByPostIdAndType(String postId, ReactionBaseDocument.ReactionType type);
    Page<PostReaction> findByPostIdAndTypeAndAnalysedFalse(String postId, ReactionBaseDocument.ReactionType type, Pageable pageable);
    Page<PostReaction> findByPostIdAndType(String postId, ReactionBaseDocument.ReactionType type, Pageable pageable);
    List<PostReaction> findByUserId(String userId);
    List<PostReaction> findByUserIdAndPostIdAndType(String userId, String postId, ReactionBaseDocument.ReactionType type);
    List<PostReaction> findByUserIdAndType(String userId, ReactionBaseDocument.ReactionType type);
    List<PostReaction> findByTypeNotAndUserIdAndPostId( ReactionBaseDocument.ReactionType type, String userId, String postId);
    long countByUserIdAndPostIdAndType(String userId, String postId, ReactionBaseDocument.ReactionType type);
    long countByPostIdAndType(String postId, ReactionBaseDocument.ReactionType type);
    void deleteByPostIdAndUserIdAndType(String postId, String userId, ReactionType type);
    // Custom queries that updates the analysed field
    @Query(value = "{ 'postId': ?0, 'type': ?1, 'analysed': false }", count = true)
    long countByPostIdAndTypeAndAnalysedFalse(String postId, ReactionBaseDocument.ReactionType type);
    
    // Analysis support methods
    long countByType(ReactionBaseDocument.ReactionType type);
    List<PostReaction> findByPostIdAndTypeAndCreatedAtBetween(String postId, ReactionBaseDocument.ReactionType type, Instant startDate, Instant endDate);
    List<PostReaction> findByTypeAndCreatedAtBetween(ReactionBaseDocument.ReactionType type, Instant startDate, Instant endDate);
}
