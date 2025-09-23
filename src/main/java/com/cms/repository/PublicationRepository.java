package com.cms.repository;

import com.cms.model.Publication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PublicationRepository extends MongoRepository<Publication, String> {
    
    @Query("{ $and: [ " +
           "{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, " +
           "         { 'content': { $regex: ?0, $options: 'i' } }, " +
           "         { 'author': { $regex: ?0, $options: 'i' } } ] }, " +
           "?1 ] }")
    List<Publication> findBySearchAndFilters(String search, Query filters);
    
    @Query("{ $and: [ " +
           "{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, " +
           "         { 'content': { $regex: ?0, $options: 'i' } }, " +
           "         { 'author': { $regex: ?0, $options: 'i' } } ] }, " +
           "?1 ] }")
    Page<Publication> findBySearchAndFilters(String search, Query filters, Pageable pageable);
    
    List<Publication> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Publication> findByFeatured(Boolean featured);
    
    @Query("{ 'date': { $gte: ?0, $lt: ?1 } }")
    List<Publication> findByYear(LocalDateTime startOfYear, LocalDateTime startOfNextYear);

    long countByDate(LocalDateTime year);
    long countByUpdatedAtBetween(Instant start, Instant end);
    
    // Count methods for dashboard
    long countByDateBetween(LocalDateTime start, LocalDateTime end);
    long countByFeatured(Boolean featured);
    long count();
}