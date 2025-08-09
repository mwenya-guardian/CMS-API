package com.cms.service;

import com.cms.dto.request.BulletinRequest;
import com.cms.dto.response.PageResponse;
import com.cms.model.Bulletin;
import com.cms.model.Bulletin.PublicationStatus;
import com.cms.model.User;
import com.cms.repository.BulletinRepository;
import com.cms.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BulletinService {

    // spring will inject these via constructor
    private final BulletinRepository bulletinRepository;
    private final MongoTemplate mongoTemplate;
    private AuthService authService;

    /**
     * retrieve all bulletins optionally filtered by date, status, author or free-text search
     * orders results by bulletinDate descending
     */
    public List<Bulletin> getAllBulletins(
            LocalDate date,
            PublicationStatus status,
            String authorId,
            String search
    ) {

        Query query = buildQuery(date, status, authorId, search);
        query.with(Sort.by(Sort.Direction.DESC, "bulletinDate"));
        return mongoTemplate.find(query, Bulletin.class);
    }

    /**
     * retrieve paginated bulletins with same filters.
     * returns a PageResponse wrapping the list, current page, page size, and total count
     */
    public PageResponse<Bulletin> getBulletinsPaginated(
            int page,
            int limit,
            LocalDate date,
            PublicationStatus status,
            String authorId,
            String search
    ) {
        Query query = buildQuery(date, status, authorId, search);
        long total = mongoTemplate.count(query, Bulletin.class);

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "bulletinDate"));
        query.with(pageable);

        List<Bulletin> items = mongoTemplate.find(query, Bulletin.class);
        return new PageResponse<>(items, page, limit, total);
    }

    /**
     * find one bulletin by its id, wrapped in optional
     */
    public Optional<Bulletin> getBulletinById(String id) {
        return bulletinRepository.findById(id);
    }

    /**
     * create a new bulletin from the request dto
     */
    public Bulletin createBulletin(BulletinRequest request) {
        Bulletin b = new Bulletin();
        applyRequestToBulletin(b, request);
        return bulletinRepository.save(b);
    }

    /**
     * update existing bulletin; throws if not found
     */
    public Bulletin updateBulletin(String id, BulletinRequest request) {
        Bulletin b = bulletinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("bulletin not found"));
        applyRequestToBulletin(b, request);
        return bulletinRepository.save(b);
    }
    public Bulletin updateStatus(String id, PublicationStatus status){
        Bulletin b = bulletinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("bulletin not found"));
        b.setStatus(status);
        return bulletinRepository.save(b);
    }

    /**
     * delete by id; throws if not found
     */
    public void deleteBulletin(String id) {
        if (!bulletinRepository.existsById(id)) {
            throw new RuntimeException("bulletin not found");
        }
        bulletinRepository.deleteById(id);
    }

    /**
     * fetch multiple bulletins by list of ids
     */
    public List<Bulletin> getBulletinsByIds(List<String> ids) {
        return bulletinRepository.findAllById(ids);
    }

    /**
     * build a mongo query from optional filters
     */
    private Query buildQuery(
            LocalDate date,
            PublicationStatus status,
            String authorId,
            String search
    ) {
        Query query = new Query();
        try {
            authService.getCurrentUser();
        } catch (Exception e) {
            status = PublicationStatus.PUBLISHED;
        }

        
        // filter by bulletinDate exactly
        if (date != null) {
            // next day exclusive
            Instant start = date.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
            Instant end = date.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
            query.addCriteria(Criteria.where("bulletinDate").gte(start).lt(end));
        }

        // filter by publication status enum
        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        // filter by author reference id
        if (authorId != null && !authorId.isEmpty()) {
            query.addCriteria(Criteria.where("author.$id").is(authorId));
        }

        // free-text search on title or content
        if (search != null && !search.isEmpty()) {
            Criteria text = new Criteria().orOperator(
                    Criteria.where("title").regex(search, "i"),
                    Criteria.where("content").regex(search, "i")
            );
            query.addCriteria(text);
        }

        return query;
    }

    /**
     * copy fields from dto into bulletin model
     */
    private void applyRequestToBulletin(Bulletin bulletin, BulletinRequest req) {
        bulletin.setTitle(req.getTitle());
        bulletin.setBulletinDate(req.getBulletinDate());
        bulletin.setContent(req.getContent());
        bulletin.setStatus(req.getStatus());
        bulletin.setPublishedAt(req.getPublishedAt());
        bulletin.setScheduledPublishAt(req.getScheduledPublishAt());
        bulletin.setAnnouncements(req.getAnnouncements());
        bulletin.setSchedules(req.getSchedules());
        bulletin.setOnDutyList(req.getOnDuty());
        bulletin.setCover(req.getCover());

        bulletin.setAuthor(new User(authService.getCurrentUser().getId()));
        // if(bulletin.getAnnouncements() != null && req.getAnnouncements() != null){
        //     bulletin.getAnnouncements().addAll(req.getAnnouncements());
        // }else {
        //     bulletin.setAnnouncements(req.getAnnouncements());
        // }
        // if(bulletin.getOnDutyList() != null && req.getOnDuty() != null){
        //     bulletin.getOnDutyList().addAll(req.getOnDuty());
        // }else {
        //     bulletin.setSchedules(req.getSchedules());
        // }
        // if(bulletin.getSchedules() != null && req.getSchedules() != null){
        //     bulletin.getSchedules().addAll(req.getSchedules());
        // }else {
        //     bulletin.setSchedules(req.getSchedules());
        // }
    }
}
