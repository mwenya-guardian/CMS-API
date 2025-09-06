package com.cms.service;

import com.cms.dto.request.QuoteRequest;
import com.cms.dto.response.FileUploadResponse;
import com.cms.dto.response.PageResponse;
import com.cms.model.Quote;
import com.cms.repository.QuoteRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
//@NoArgsConstructor never add this to a service it make the dependencies null during injection
public class QuoteService {
    

    private QuoteRepository quoteRepository;
    private MongoTemplate mongoTemplate;
    private FileService fileService;
    
    public List<Quote> getAllQuotes(Integer year, Integer month, Integer day, 
                                  String category, Boolean featured, String search) {
        Query query = buildQuery(year, month, day, category, featured, search);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        return mongoTemplate.find(query, Quote.class);
    }
    
    public PageResponse<Quote> getQuotesPaginated(int page, int limit, Integer year, 
                                                Integer month, Integer day, String category, 
                                                Boolean featured, String search) {
        Query query = buildQuery(year, month, day, category, featured, search);
        long total = mongoTemplate.count(query, Quote.class);
        
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        query.with(pageable);
        
        List<Quote> quotes = mongoTemplate.find(query, Quote.class);
        
        return new PageResponse<>(quotes, page, limit, total);
    }
    
    public Optional<Quote> getQuoteById(String id) {
        return quoteRepository.findById(id);
    }
    
    public Quote createQuote(QuoteRequest request) throws IOException {
        Quote quote = new Quote();
        updateQuoteFromRequest(quote, request);
        return quoteRepository.save(quote);
    }
    
    public Quote updateQuote(String id, QuoteRequest request) throws IOException {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        
        updateQuoteFromRequest(quote, request);
        return quoteRepository.save(quote);
    }
    
    public void deleteQuote(String id) {
        if (!quoteRepository.existsById(id)) {
            throw new RuntimeException("Quote not found");
        }
        quoteRepository.deleteById(id);
    }
    
    public List<Quote> getQuotesByIds(List<String> ids) {
        return quoteRepository.findAllById(ids);
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
            
            query.addCriteria(Criteria.where("createdAt").gte(startDate).lt(endDate));
        }
        
        if (category != null && !category.isEmpty()) {
            query.addCriteria(Criteria.where("category").regex(category, "i"));
        }
        
        if (featured != null) {
            query.addCriteria(Criteria.where("featured").is(featured));
        }
        
        if (search != null && !search.isEmpty()) {
            Criteria searchCriteria = new Criteria().orOperator(
                Criteria.where("text").regex(search, "i"),
                Criteria.where("author").regex(search, "i"),
                Criteria.where("source").regex(search, "i")
            );
            query.addCriteria(searchCriteria);
        }
        
        return query;
    }
    
    private void updateQuoteFromRequest(Quote quote, QuoteRequest request) throws IOException {
        quote.setText(request.getText());
        quote.setAuthor(request.getAuthor());
        quote.setSource(request.getSource());
        quote.setCategory(request.getCategory());
        if (request.getFeatured() != null) {
            quote.setFeatured(request.getFeatured());
        }
        if(request.getImageUrl() == null || request.getImageUrl().isBlank()){
            quote.setImageUrl(request.getImageUrl());
            if(quote.getImageUrl() != null && !quote.getImageUrl().isBlank())
                fileService.deleteFile(quote.getImageUrl());
        }
    }
    public FileUploadResponse uploadImage(MultipartFile file, Boolean isPublic) throws IOException {
        return fileService.uploadImage(file, isPublic);
    }
        
}