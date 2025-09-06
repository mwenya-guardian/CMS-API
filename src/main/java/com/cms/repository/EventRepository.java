package com.cms.repository;

import com.cms.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    
    @Query("{ $and: [ " +
           "{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, " +
           "         { 'description': { $regex: ?0, $options: 'i' } }, " +
           "         { 'location': { $regex: ?0, $options: 'i' } } ] }, " +
           "?1 ] }")
    List<Event> findBySearchAndFilters(String search, Query filters);
    
    @Query("{ $and: [ " +
           "{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, " +
           "         { 'description': { $regex: ?0, $options: 'i' } }, " +
           "         { 'location': { $regex: ?0, $options: 'i' } } ] }, " +
           "?1 ] }")
    Page<Event> findBySearchAndFilters(String search, Query filters, Pageable pageable);
    
    List<Event> findByStartDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Event> findByFeatured(Boolean featured);
    List<Event> findByCategory(Event.EventCategory category);

    long countByUpdatedAtBetween(Instant start, Instant end);
    long countByCategory(Event.EventCategory category);
    long countByStartDate(LocalDateTime year);
}