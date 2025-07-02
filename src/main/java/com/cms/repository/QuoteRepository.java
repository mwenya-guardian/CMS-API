package com.cms.repository;

import com.cms.model.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuoteRepository extends MongoRepository<Quote, String> {
    
    @Query("{ $and: [ " +
           "{ $or: [ { 'text': { $regex: ?0, $options: 'i' } }, " +
           "         { 'author': { $regex: ?0, $options: 'i' } }, " +
           "         { 'source': { $regex: ?0, $options: 'i' } } ] }, " +
           "?1 ] }")
    List<Quote> findBySearchAndFilters(String search, Query filters);
    
    @Query("{ $and: [ " +
           "{ $or: [ { 'text': { $regex: ?0, $options: 'i' } }, " +
           "         { 'author': { $regex: ?0, $options: 'i' } }, " +
           "         { 'source': { $regex: ?0, $options: 'i' } } ] }, " +
           "?1 ] }")
    Page<Quote> findBySearchAndFilters(String search, Query filters, Pageable pageable);
    
    List<Quote> findByFeatured(Boolean featured);
    
    List<Quote> findByCategory(String category);
    
    List<Quote> findByAuthor(String author);
}