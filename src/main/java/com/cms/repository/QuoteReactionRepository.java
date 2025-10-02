package com.cms.repository;

import com.cms.model.QuoteReaction;
import com.cms.model.ReactionBaseDocument;
import com.cms.model.ReactionBaseDocument.ReactionType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface QuoteReactionRepository extends MongoRepository<QuoteReaction, String> {

    // find all reactions for a specific quote
    List<QuoteReaction> findByQuoteId(String quoteId);
    List<QuoteReaction> findByUserId(String userId);
    List<QuoteReaction> findByQuoteIdAndType(String quoteId, ReactionBaseDocument.ReactionType type);
    Page<QuoteReaction> findByQuoteIdAndTypeAndAnalysedFalse(String quoteId, ReactionBaseDocument.ReactionType type, Pageable page);
    Page<QuoteReaction> findByQuoteIdAndType(String quoteId, ReactionBaseDocument.ReactionType type, Pageable page);
    List<QuoteReaction> findByUserIdAndQuoteIdAndType(String userId, String quoteId, ReactionBaseDocument.ReactionType type);
    List<QuoteReaction> findByTypeNotAndUserIdAndQuoteId(ReactionBaseDocument.ReactionType type, String userId, String quoteId);
    List<QuoteReaction> findByUserIdAndType(String userId, ReactionBaseDocument.ReactionType type);
    long countByUserIdAndQuoteIdAndType(String userId, String quoteId, ReactionBaseDocument.ReactionType type);
    long countByQuoteIdAndType(String quoteId, ReactionBaseDocument.ReactionType type);
    void deleteByQuoteIdAndUserIdAndType(String quoteId, String userId, ReactionType type);
    // Custom queries that updates the analysed field
    @Query(value = "{ 'quoteId': ?0, 'type': ?1, 'analysed': false }", count = true)
    long countByQuoteIdAndTypeAndAnalysedFalse(String quoteId, ReactionBaseDocument.ReactionType type);
    
    // Analysis support methods
    long countByType(ReactionBaseDocument.ReactionType type);
    List<QuoteReaction> findByQuoteIdAndTypeAndCreatedAtBetween(String quoteId, ReactionBaseDocument.ReactionType type, Instant startDate, Instant endDate);
    List<QuoteReaction> findByTypeAndCreatedAtBetween(ReactionBaseDocument.ReactionType type, Instant startDate, Instant endDate);
}
