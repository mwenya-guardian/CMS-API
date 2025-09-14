package com.cms.repository;

import com.cms.model.QuoteReaction;
import com.cms.model.ReactionBaseDocument;
import com.cms.model.ReactionBaseDocument.ReactionType;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuoteReactionRepository extends MongoRepository<QuoteReaction, String> {

    // find all reactions for a specific quote
    List<QuoteReaction> findByQuoteId(String quoteId);
    List<QuoteReaction> findByUserId(String userId);
    List<QuoteReaction> findByQuoteIdAndType(String quoteId, ReactionBaseDocument.ReactionType type);
    List<QuoteReaction> findByUserIdAndQuoteIdAndType(String userId, String quoteId, ReactionBaseDocument.ReactionType type);
    List<QuoteReaction> findByTypeNotAndUserIdAndQuoteId(ReactionBaseDocument.ReactionType type, String userId, String quoteId);
    long countByUserIdAndQuoteIdAndType(String userId, String quoteId, ReactionBaseDocument.ReactionType type);
    long countByQuoteIdAndType(String quoteId, ReactionBaseDocument.ReactionType type);
    void deleteByQuoteIdAndUserIdAndType(String quoteId, String userId, ReactionType type);

}
