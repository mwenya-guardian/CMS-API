package com.cms.controller;

import com.cms.dto.request.NewsletterSubscriberRequest;
import com.cms.dto.response.ApiResponse;
import com.cms.dto.response.NewsletterSubscriberResponse;
import com.cms.dto.response.PageResponse;
import com.cms.model.NewsletterSubscriber;
import com.cms.service.NewsletterSubscriberService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/newsletter-subscribers")
@AllArgsConstructor
public class NewsletterSubscriberController {

    private final NewsletterSubscriberService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NewsletterSubscriberResponse>>> getAll() {
        List<NewsletterSubscriberResponse> list = service.findAll();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PageResponse<NewsletterSubscriberResponse>>> getPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<NewsletterSubscriberResponse> pg = service.findPaginated(page, size);
        return ResponseEntity.ok(ApiResponse.success(pg));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<NewsletterSubscriberResponse>>> getActive(
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(dir, sortBy);
        List<NewsletterSubscriberResponse> list = service.findActiveSorted(sort);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsletterSubscriberResponse>> getById(@PathVariable String id) {
        NewsletterSubscriberResponse subscriberResponse = service.getById(id)
                .orElseThrow(() -> new RuntimeException("Newsletter subscriber not found"));
        return ResponseEntity.ok(ApiResponse.success(subscriberResponse));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<NewsletterSubscriberResponse>> getByEmail(@PathVariable String email) {
        NewsletterSubscriberResponse subscriber = service.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Newsletter subscriber not found"));
        return ResponseEntity.ok(ApiResponse.success(subscriber));
    }

    @PostMapping("/subscribe")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NewsletterSubscriberResponse>> subscribe(
            @Valid @RequestBody NewsletterSubscriberRequest request
    ) throws MessagingException, IOException {
        NewsletterSubscriberResponse s = service.subscribe(request.getEmail());
        // optionally: send verification email with s.getVerificationToken()
        return ResponseEntity.ok(ApiResponse.success(s));
    }
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verify(@RequestParam String token, @RequestParam String email) {
        boolean ok = service.verifyToken(email, token);
        return ResponseEntity.ok(ApiResponse.success(ok));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<NewsletterSubscriberResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody NewsletterSubscriber request
    ) {
        NewsletterSubscriberResponse updated = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
