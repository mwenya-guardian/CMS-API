package com.cms.service;

import com.cms.model.NewsletterSchedule;
import com.cms.model.Bulletin;
import com.cms.model.logs.NewsletterSendLog;
import com.cms.repository.NewsletterScheduleRepository;
import com.cms.repository.NewsletterSendLogRepository;
import com.cms.repository.NewsletterSubscriberRepository;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@AllArgsConstructor
public class NewsletterScheduler {

    private final NewsletterScheduleRepository scheduleRepo;
    private final TaskScheduler taskScheduler;
    private final BulletinService bulletinService;
    private final ModelToHtmlService ModelToHtmlService;
    private final EmailService emailService;
    private final NewsletterSubscriberRepository subscriberRepository;
    private final NewsletterSendLogRepository logRepo;

    // track scheduled futures to allow cancellation/reschedule
    private final Map<String, ScheduledFuture<?>> jobs = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // schedule all enabled schedules on startup
        List<NewsletterSchedule> schedules = scheduleRepo.findAll();
        schedules.forEach(this::schedule);
    }

    public void schedule(NewsletterSchedule s) {
        if (!s.isEnabled()) return;

        CronTrigger trigger = new CronTrigger(s.getCronExpression(), java.util.TimeZone.getTimeZone(s.getZoneId()));
        ScheduledFuture<?> future = taskScheduler.schedule(() -> runSchedule(s.getId()), trigger);
        jobs.put(s.getId(), future);
    }

    public void cancel(String scheduleId) {
        ScheduledFuture<?> f = jobs.remove(scheduleId);
        if (f != null) f.cancel(false);
    }

    public void reschedule(NewsletterSchedule s) {
        cancel(s.getId());
        schedule(s);
    }

    // core runner
    public void runSchedule(String scheduleId) {
        NewsletterSchedule s = scheduleRepo.findById(scheduleId).orElse(null);
        if (s == null || !s.isEnabled()) return;
        
        try {
            // load bulletins & generate combined PDF
            // For simplicity assume one bulletin id; you can merge multiple PDF bytes if needed
            byte[] pdfBytes = null;
            String bulletinHTML = null;
            if (s.getBulletinIds() != null && !s.getBulletinIds().isEmpty()) {
                Bulletin bulletin = bulletinService.getBulletinById(s.getBulletinIds().getFirst())
                        .orElse(new Bulletin());
                bulletinHTML = ModelToHtmlService.generateBulletinHtml(bulletin);
            }

            // fetch subscribers in pages and send
            int success = 0;
            int failure = 0;
            int page = 0;
            int size = 200; // batch size to avoid provider limits
            while (true) {
                var pageRes = subscriberRepository.findByActiveTrueAndVerifiedTrue(PageRequest.of(page, size));
                if (!pageRes.hasContent()) break;
                for (var sub : pageRes.getContent()) {
                    try {
                        // consider personalizing subject/body, include unsubscribe link
                        String subject = s.getTitle();
                        emailService.sendHtml(sub.getEmail(), subject, bulletinHTML);
                        success++;
                        // small sleep to avoid hitting provider rate limits (optional)
                        Thread.sleep(25);
                    } catch (Exception ex) {
                        failure++;
                        // log individually, consider retry strategy
                    }
                }
                if (!pageRes.hasNext()) break;
                page++;
            }

            // persist log
            NewsletterSendLog log = new NewsletterSendLog();
            log.setScheduleId(scheduleId);
            log.setSentAt(Instant.now());
            log.setSuccessCount(success);
            log.setFailureCount(failure);
            logRepo.save(log);

            // update schedule metadata
            s.setLastRunAt(Instant.now());
            scheduleRepo.save(s);
        } catch (Exception ex) {
            // log and continue
            System.out.println(ex.getMessage());
            // optional: send alert to admin
        }
    }
}
