package com.cms.repository;

import com.cms.model.PostReaction;
import com.cms.model.ReactionBaseDocument;
import com.cms.model.ReactionBaseDocument.ReactionType;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PostReactionRepository extends MongoRepository<PostReaction, String> {
    // find all reactions for a specific post
    List<PostReaction> findByPostId(String postId);
    List<PostReaction> findByPostIdAndType(String postId, ReactionBaseDocument.ReactionType type);
    List<PostReaction> findByUserId(String userId);
    List<PostReaction> findByUserIdAndPostIdAndType(String userId, String postId, ReactionBaseDocument.ReactionType type);
    List<PostReaction> findByUserIdAndType(String userId, ReactionBaseDocument.ReactionType type);
    List<PostReaction> findByTypeNotAndUserIdAndPostId( ReactionBaseDocument.ReactionType type, String userId, String postId);
    long countByUserIdAndPostIdAndType(String userId, String postId, ReactionBaseDocument.ReactionType type);
    long countByPostIdAndType(String postId, ReactionBaseDocument.ReactionType type);
    void deleteByPostIdAndUserIdAndType(String postId, String userId, ReactionType type);
}
