package com.cms.repository;

import com.cms.model.QuoteReaction;
import com.cms.model.ReactionBaseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuoteReactionRepository extends MongoRepository<QuoteReaction, String> {

    // find all reactions for a specific quote
    List<QuoteReaction> findByQuoteId(String quoteId);
    List<QuoteReaction> findByUserId(String userId);
    List<QuoteReaction> findByUserIdAndQuoteIdAndType(String userId, String quoteId, ReactionBaseDocument.ReactionType type);
    long countByUserIdAndQuoteIdAndType(String userId, String quoteId, ReactionBaseDocument.ReactionType type);
    long countByQuoteIdAndType(String quoteId, ReactionBaseDocument.ReactionType type);

}
