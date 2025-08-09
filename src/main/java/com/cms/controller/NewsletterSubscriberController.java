package com.cms.controller;

import com.cms.dto.response.ApiResponse;
import com.cms.model.NewsletterSubscriber;
import com.cms.service.NewsletterSubscriberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/newsletter-subscribers")
@AllArgsConstructor
public class NewsletterSubscriberController {

    private final NewsletterSubscriberService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NewsletterSubscriber>>> getAll() {
        List<NewsletterSubscriber> list = service.getAll();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<NewsletterSubscriber>>> getAllActive() {
        List<NewsletterSubscriber> list = service.getAllActive();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsletterSubscriber>> getById(@PathVariable String id) {
        NewsletterSubscriber sub = service.getById(id)
                .orElseThrow(() -> new RuntimeException("subscriber not found"));
        return ResponseEntity.ok(ApiResponse.success(sub));
    }

    @GetMapping("/by-email")
    public ResponseEntity<ApiResponse<NewsletterSubscriber>> getByEmail(@RequestParam String email) {
        NewsletterSubscriber sub = service.getByEmail(email)
                .orElseThrow(() -> new RuntimeException("subscriber not found"));
        return ResponseEntity.ok(ApiResponse.success(sub));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NewsletterSubscriber>> create(
            @Valid @RequestBody NewsletterSubscriber request
    ) {
        NewsletterSubscriber created = service.create(request);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsletterSubscriber>> update(
            @PathVariable String id,
            @Valid @RequestBody NewsletterSubscriber request
    ) {
        NewsletterSubscriber updated = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
