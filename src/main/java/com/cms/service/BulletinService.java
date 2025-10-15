package com.cms.service;

import com.cms.dto.request.BulletinRequest;
import com.cms.dto.response.BulletinSummary;
import com.cms.dto.response.PageResponse;
import com.cms.model.Bulletin;
import com.cms.model.Bulletin.PublicationStatus;
import com.cms.model.User;
import com.cms.repository.BulletinRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import jakarta.annotation.PostConstruct;

@Service
@AllArgsConstructor
public class BulletinService {

    // spring will inject these via constructor
    private final BulletinRepository bulletinRepository;
    private final MongoTemplate mongoTemplate;
    private final TaskScheduler taskScheduler;
    private AuthService authService;

    // Track scheduled publication jobs by bulletin id so we can cancel/reschedule
    private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledPublications = new ConcurrentHashMap<>();

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
        Bulletin saved = bulletinRepository.save(b);
        schedulePublicationIfNeeded(saved);
        return saved;
    }

    /**
     * update existing bulletin; throws if not found
     */
    public Bulletin updateBulletin(String id, BulletinRequest request) {
        Bulletin b = bulletinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("bulletin not found"));
        applyRequestToBulletin(b, request);
        Bulletin saved = bulletinRepository.save(b);
        // Reschedule/cancel depending on current status/time
        schedulePublicationIfNeeded(saved);
        return saved;
    }

    public Bulletin updateStatus(String id, PublicationStatus status){
        Bulletin b = bulletinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("bulletin not found"));
        if(status == PublicationStatus.PUBLISHED){
            maintainStatus();
        }
        b.setStatus(status);
        Bulletin saved = bulletinRepository.save(b);
        if (status == PublicationStatus.SCHEDULED) {
            schedulePublicationIfNeeded(saved);
        } else {
            cancelScheduledPublication(saved.getId());
        }
        return saved;
    }

    /**
     * delete by id; throws if not found
     */
    public void deleteBulletin(String id) {
        if (!bulletinRepository.existsById(id)) {
            throw new RuntimeException("bulletin not found");
        }
        bulletinRepository.deleteById(id);
        cancelScheduledPublication(id);
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
        bulletin.setScheduledPublishAt(req.getScheduledPublishAt());
        bulletin.setAnnouncements(req.getAnnouncements());
        bulletin.setSchedules(req.getSchedules());
        bulletin.setOnDutyList(req.getOnDutyList());
        bulletin.setCover(req.getCover());

        bulletin.setAuthor(new User(authService.getCurrentUser().getId()));
        if(req.getStatus() == PublicationStatus.PUBLISHED){
            maintainStatus();
        }
        bulletin.setStatus(req.getStatus());

    }
    private void maintainStatus(){
        List<Bulletin> publichedBulletinList = bulletinRepository.findByStatus(PublicationStatus.PUBLISHED);
        List<Bulletin> updated = publichedBulletinList.stream()
                .peek((savedBulletin)-> savedBulletin.setStatus(PublicationStatus.DRAFT))
                .toList();
        bulletinRepository.saveAll(updated);
    }

    /**
     * Schedule auto-publication for the given bulletin if it is SCHEDULED and has a future publish time.
     * If a job already exists for this bulletin, it will be cancelled and replaced.
     */
    private void schedulePublicationIfNeeded(Bulletin bulletin) {
        try {
            if (bulletin == null) return;
            if (bulletin.getStatus() != PublicationStatus.SCHEDULED) {
                cancelScheduledPublication(bulletin.getId());
                return;
            }

            Date scheduledAtDate = bulletin.getScheduledPublishAt();
            if (scheduledAtDate == null) {
                cancelScheduledPublication(bulletin.getId());
                return;
            }

            Instant scheduledAt = scheduledAtDate.toInstant();
            if (scheduledAt.isBefore(Instant.now())) {
                // If the scheduled time is in the past, publish immediately
                updateStatus(bulletin.getId(), PublicationStatus.PUBLISHED);
                return;
            }

            // Cancel existing if any
            cancelScheduledPublication(bulletin.getId());

            ScheduledFuture<?> future = taskScheduler.schedule(
                    () -> {
                        try {
                            updateStatus(bulletin.getId(), PublicationStatus.PUBLISHED);
                        } catch (Exception ignored) {
                        } finally {
                            // Remove reference after execution
                            scheduledPublications.remove(bulletin.getId());
                        }
                    },
                    scheduledAt
            );
            if (future != null) {
                scheduledPublications.put(bulletin.getId(), future);
            }
        } catch (Exception ignored) {
            // avoid breaking main flow due to scheduling errors
        }
    }

    private void cancelScheduledPublication(String bulletinId) {
        if (bulletinId == null) return;
        ScheduledFuture<?> existing = scheduledPublications.remove(bulletinId);
        if (existing != null) {
            existing.cancel(false);
        }
    }

    /**
     * On startup, re-schedule all existing SCHEDULED bulletins.
     */
    @PostConstruct
    private void rescheduleExistingScheduledBulletins() {
        List<Bulletin> scheduled = bulletinRepository.findByStatus(PublicationStatus.SCHEDULED);
        for (Bulletin bulletin : scheduled) {
            schedulePublicationIfNeeded(bulletin);
        }
    }

    /**
     * Get published and scheduled bulletin summaries (ID and title only)
     * @return List of BulletinSummary containing only ID and title
     */
    public List<BulletinSummary> getPublishedBulletinSummaries() {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").in(PublicationStatus.PUBLISHED, PublicationStatus.SCHEDULED));
        query.fields().include("id", "title");
        query.with(Sort.by(Sort.Direction.DESC, "bulletinDate"));
        
        List<Bulletin> bulletins = mongoTemplate.find(query, Bulletin.class);
        return bulletins.stream()
                .map(bulletin -> new BulletinSummary(bulletin.getId(), bulletin.getTitle()))
                .toList();
    }
    
    // Count methods for dashboard
    public long getTotalCount() {
        return bulletinRepository.count();
    }
    
    public long getCountByYear(int year) {
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime startOfNextYear = LocalDateTime.of(year + 1, 1, 1, 0, 0);
        Instant startInstant = startOfYear.atZone(java.time.ZoneId.systemDefault()).toInstant();
        Instant endInstant = startOfNextYear.atZone(java.time.ZoneId.systemDefault()).toInstant();
        return bulletinRepository.countByCreatedAtBetween(startInstant, endInstant);
    }
    
    public long getPublishedCount() {
        return bulletinRepository.countByStatus(PublicationStatus.PUBLISHED);
    }
}
