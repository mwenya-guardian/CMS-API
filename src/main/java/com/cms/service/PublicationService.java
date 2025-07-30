package com.cms.service;

import com.cms.dto.request.PublicationRequest;
import com.cms.dto.response.PageResponse;
import com.cms.model.Publication;
import com.cms.repository.PublicationRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@NoArgsConstructor
@AllArgsConstructor
public class PublicationService {

    private PublicationRepository publicationRepository;
    private MongoTemplate mongoTemplate;
    
    public List<Publication> getAllPublications(Integer year, Integer month, Integer day, 
                                              String category, Boolean featured, String search) {
        Query query = buildQuery(year, month, day, category, featured, search);
        query.with(Sort.by(Sort.Direction.DESC, "date"));
        return mongoTemplate.find(query, Publication.class);
    }
    
    public PageResponse<Publication> getPublicationsPaginated(int page, int limit, Integer year, 
                                                            Integer month, Integer day, String category, 
                                                            Boolean featured, String search) {
        Query query = buildQuery(year, month, day, category, featured, search);
        long total = mongoTemplate.count(query, Publication.class);
        
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "date"));
        query.with(pageable);
        
        List<Publication> publications = mongoTemplate.find(query, Publication.class);
        
        return new PageResponse<>(publications, page, limit, total);
    }
    
    public Optional<Publication> getPublicationById(String id) {
        return publicationRepository.findById(id);
    }
    
    public List<Publication> getPublicationsByYear(int year) {
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime startOfNextYear = LocalDateTime.of(year + 1, 1, 1, 0, 0);
        return publicationRepository.findByYear(startOfYear, startOfNextYear);
    }
    
    public Publication createPublication(PublicationRequest request) {
        Publication publication = new Publication();
        updatePublicationFromRequest(publication, request);
        return publicationRepository.save(publication);
    }
    
    public Publication updatePublication(String id, PublicationRequest request) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publication not found"));
        
        updatePublicationFromRequest(publication, request);
        return publicationRepository.save(publication);
    }
    
    public void deletePublication(String id) {
        if (!publicationRepository.existsById(id)) {
            throw new RuntimeException("Publication not found");
        }
        publicationRepository.deleteById(id);
    }
    
    public List<Publication> getPublicationsByIds(List<String> ids) {
        return publicationRepository.findAllById(ids);
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
            
            query.addCriteria(Criteria.where("date").gte(startDate).lt(endDate));
        }
        
        if (category != null && !category.isEmpty()) {
            query.addCriteria(Criteria.where("tags").in(category));
        }
        
        if (featured != null) {
            query.addCriteria(Criteria.where("featured").is(featured));
        }
        
        if (search != null && !search.isEmpty()) {
            Criteria searchCriteria = new Criteria().orOperator(
                Criteria.where("title").regex(search, "i"),
                Criteria.where("content").regex(search, "i"),
                Criteria.where("author").regex(search, "i")
            );
            query.addCriteria(searchCriteria);
        }
        
        return query;
    }
    
    private void updatePublicationFromRequest(Publication publication, PublicationRequest request) {
        publication.setTitle(request.getTitle());
        publication.setContent(request.getContent());
        publication.setImageUrl(request.getImageUrl());
        publication.setDate(request.getDate());
        publication.setLayoutType(request.getLayoutType());
        publication.setAuthor(request.getAuthor());
        publication.setTags(request.getTags());
        if (request.getFeatured() != null) {
            publication.setFeatured(request.getFeatured());
        }
    }
}