package com.cms.service;

import com.cms.dto.request.EventRequest;
import com.cms.dto.response.ApiResponse;
import com.cms.dto.response.FileUploadResponse;
import com.cms.dto.response.PageResponse;
import com.cms.model.Event;
import com.cms.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
//@NoArgsConstructor not to be added
@AllArgsConstructor
public class EventService {
    private EventRepository eventRepository;
    private MongoTemplate mongoTemplate;
    private FileService fileService;
    
    public List<Event> getAllEvents(Integer year, Integer month, Integer day, 
                                  String category, Boolean featured, String search) {
        Query query = buildQuery(year, month, day, category, featured, search);
        query.with(Sort.by(Sort.Direction.DESC, "startDate"));
        return mongoTemplate.find(query, Event.class);
    }
    
    public PageResponse<Event> getEventsPaginated(int page, int limit, Integer year, 
                                                Integer month, Integer day, String category, 
                                                Boolean featured, String search) {
        Query query = buildQuery(year, month, day, category, featured, search);
        long total = mongoTemplate.count(query, Event.class);
        
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "startDate"));
        query.with(pageable);
        
        List<Event> events = mongoTemplate.find(query, Event.class);
        
        return new PageResponse<>(events, page, limit, total);
    }
    
    public Optional<Event> getEventById(String id) {
        return eventRepository.findById(id);
    }
    
    public Event createEvent(EventRequest request) throws IOException {
        Event event = new Event();
        updateEventFromRequest(event, request);
        return eventRepository.save(event);
    }
    
    public Event updateEvent(String id, EventRequest request) throws IOException {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        updateEventFromRequest(event, request);
        return eventRepository.save(event);
    }
    
    public void deleteEvent(String id) throws IOException {
        Event event = eventRepository.findById(id).orElseThrow(()->
                new RuntimeException("Event not found")
        );
        if(event.getImageUrl() != null && !event.getImageUrl().isBlank()){
            fileService.deleteFile(event.getImageUrl());
        }
        eventRepository.deleteById(id);
    }
    
    public List<Event> getEventsByIds(List<String> ids) {
        return eventRepository.findAllById(ids);
    }
    
    private Query buildQuery(Integer year, Integer month, Integer day, String category, 
                           Boolean featured, String search) {
        Query query = new Query();
        
        if (year != null) {
            LocalDateTime startDate = LocalDateTime.of(year, month != null ? month : 1, 
                                                     day != null ? day : 1, 0, 0);
            LocalDateTime endDate;
            
            if (day != null) {
                endDate = startDate.plusDays(1);
            } else if (month != null) {
                endDate = startDate.plusMonths(1);
            } else {
                endDate = startDate.plusYears(1);
            }
            
            query.addCriteria(Criteria.where("startDate").gte(startDate).lt(endDate));
        }
        
        if (category != null && !category.isEmpty()) {
            try {
                Event.EventCategory eventCategory = Event.EventCategory.valueOf(category.toUpperCase());
                query.addCriteria(Criteria.where("category").is(eventCategory));
            } catch (IllegalArgumentException e) {
                // Invalid category, ignore filter
            }
        }
        
        if (featured != null) {
            query.addCriteria(Criteria.where("featured").is(featured));
        }
        
        if (search != null && !search.isEmpty()) {
            Criteria searchCriteria = new Criteria().orOperator(
                Criteria.where("title").regex(search, "i"),
                Criteria.where("description").regex(search, "i"),
                Criteria.where("location").regex(search, "i")
            );
            query.addCriteria(searchCriteria);
        }
        
        return query;
    }
    
    private void updateEventFromRequest(Event event, EventRequest request) throws IOException {
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        event.setCategory(request.getCategory());
        event.setFeatured(request.getFeatured());

        if(request.getImageUrl() == null || request.getImageUrl().isBlank()){
            event.setImageUrl(request.getImageUrl());
            if(event.getImageUrl() != null && !event.getImageUrl().isBlank())
                fileService.deleteFile(event.getImageUrl());
        }
    }
    public FileUploadResponse uploadImage(MultipartFile file, Boolean isPublic) throws IOException {
        return fileService.uploadImage(file, isPublic);
    }
}