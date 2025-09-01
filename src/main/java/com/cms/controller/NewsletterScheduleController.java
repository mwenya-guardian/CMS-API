package com.cms.controller;

import com.cms.model.NewsletterSchedule;
import com.cms.repository.NewsletterScheduleRepository;
import com.cms.service.NewsletterScheduler;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/newsletter-schedules")
@AllArgsConstructor
public class NewsletterScheduleController {
    private final NewsletterScheduleRepository repo;
    private final NewsletterScheduler scheduler;

    @GetMapping
    public List<NewsletterSchedule> all() { return repo.findAll(); }

    @PostMapping
    public NewsletterSchedule create(@RequestBody @Valid NewsletterSchedule schedule) {
        NewsletterSchedule saved = repo.save(schedule);
        scheduler.schedule(saved);
        return saved;
    }

    @PutMapping("/{id}")
    public NewsletterSchedule update(@PathVariable String id, @RequestBody @Valid NewsletterSchedule request) {
        request.setId(id);
        NewsletterSchedule saved = repo.save(request);
        scheduler.reschedule(saved);
        return saved;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        repo.deleteById(id);
        scheduler.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<Void> runNow(@PathVariable String id) {
        scheduler.runSchedule(id);
        return ResponseEntity.accepted().build();
    }
}
