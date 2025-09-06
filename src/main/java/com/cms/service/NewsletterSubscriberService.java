package com.cms.service;

import com.cms.dto.response.NewsletterSubscriberResponse;
import com.cms.dto.response.PageResponse;
import com.cms.model.NewsletterSubscriber;
import com.cms.repository.NewsletterSubscriberRepository;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@AllArgsConstructor
public class NewsletterSubscriberService {

    private final NewsletterSubscriberRepository repository;
    private final EmailService emailService;

    /**
     * Return all subscribers (unsorted).
     */
    public List<NewsletterSubscriberResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(this::mapEntityToResponse)
                .toList();
    }

    /**
     * Paginated listing
     */
    public PageResponse<NewsletterSubscriberResponse> findPaginated(int page, int size) {
        // convert page param (1-based in controllers) to 0-based for Spring
        int p = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(p, Math.max(1, size), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NewsletterSubscriberResponse> pageResponse = repository.findAll(pageable).map(this::mapEntityToResponse);
        return new PageResponse<NewsletterSubscriberResponse>(
                pageResponse.getContent(), pageResponse.getNumber(),
                pageResponse.getSize(), pageResponse.getTotalPages()
        );
    }

    /**
     * Return active subscribers sorted by provided Sort
     */
    public List<NewsletterSubscriberResponse> findActiveAndVerifiedSorted() {
        return repository.findByActiveTrueAndVerifiedTrue().stream().map(this::mapEntityToResponse).toList();

    }
    public List<NewsletterSubscriberResponse> findActiveSorted(Sort sort) {
        return repository.findByActiveTrue(sort).stream().map(this::mapEntityToResponse).toList();

    }
    public PageResponse<NewsletterSubscriberResponse> findActiveAndVerifiedSortedPaged(int page, int size) {
        int p = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(p, Math.max(1, size), Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<NewsletterSubscriberResponse> pageResponse = repository.findByActiveTrueAndVerifiedTrue(pageable)
                .map(this::mapEntityToResponse);
        return new PageResponse<NewsletterSubscriberResponse>(pageResponse.getContent(), pageResponse.getNumber(),
                pageResponse.getSize(),pageResponse.getTotalPages());
    }

    public Optional<NewsletterSubscriberResponse> getById(String id) {
        return repository.findById(id).map(this::mapEntityToResponse);
    }

    public Optional<NewsletterSubscriberResponse> findByEmail(String email) {
        return repository.findByEmail(email).map(this::mapEntityToResponse);
    }

    /**
     * Subscribe by email (convenience method).
     * - If the email exists and is inactive, reactivate and generate a new token.
     * - If exists and verified=false, refresh token.
     * - If not exists, create new.
     */
    public NewsletterSubscriberResponse subscribe(String email) throws MessagingException, MailException, IOException {
        String normalized = email.trim().toLowerCase();
        Optional<NewsletterSubscriber> existing = repository.findByEmail(normalized);
        NewsletterSubscriber s;
        if (existing.isPresent()) {
            s = existing.get();
            s.setActive(Boolean.TRUE);
            NewsletterSubscriber savedSubscriber = repository.save(s);
            if(!existing.get().getVerified()){
                emailService.sendPlain(normalized, "SDA Bulletin Subscription Verification",
                        "Verification Code: " + s.getVerificationToken());
            }

        } else {
            s = new NewsletterSubscriber();
            s.setEmail(normalized);
            s.setActive(Boolean.TRUE);
            s.setVerified(Boolean.FALSE);
            s.setVerificationToken(generateToken());

            NewsletterSubscriber savedSubscriber = repository.save(s);
            emailService.sendPlain(normalized, "SDA Bulletin Subscription Verification",
                    "Verification Code: " + savedSubscriber.getVerificationToken());
        }
        return mapEntityToResponse(s);
    }
    public NewsletterSubscriberResponse unsubscribe(String email) {
        String normalized = email.trim().toLowerCase();
        Optional<NewsletterSubscriber> existing = repository.findByEmail(normalized);
        NewsletterSubscriber s = existing.orElseThrow();
        s.setActive(Boolean.FALSE);
        return mapEntityToResponse(s);
    }

    /**
     * Verify subscriber by token. If token matches, mark verified = true and clear the token.
     * Returns true if verification succeeded.
     */
    public boolean verifyToken(String email, String token ) {
        if (token == null || token.isBlank()) return false;
        NewsletterSubscriber subscriber = repository.findByEmail(email).orElseThrow();
        if(subscriber.getVerified())
            return true;

        if (token.equals(subscriber.getVerificationToken())) {
            subscriber.setVerified(Boolean.TRUE);
            subscriber.setVerificationToken(null);
            repository.save(subscriber);
            return true;
        }

        return false;
    }

    public NewsletterSubscriberResponse update(String id, NewsletterSubscriber request) {
        NewsletterSubscriber existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Newsletter subscriber not found"));
        // Only allow specific updatable fields
        if (request.getEmail() != null) existing.setEmail(request.getEmail().trim().toLowerCase());
        if (request.getActive() != null) existing.setActive(request.getActive());
        if (request.getVerified() != null) existing.setVerified(request.getVerified());
        if (request.getVerificationToken() != null) existing.setVerificationToken(request.getVerificationToken());
        NewsletterSubscriber savedSubscriber = repository.save(existing);
        return mapEntityToResponse(savedSubscriber);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    private String generateToken() {
        return String.valueOf(new Random().nextInt(100000, 999999));
    }

    private NewsletterSubscriberResponse mapEntityToResponse(NewsletterSubscriber subscriber){
        return new NewsletterSubscriberResponse(subscriber.getId(), subscriber.getEmail(),
                subscriber.getActive(),
                subscriber.getVerified(), subscriber.getCreatedAt(), subscriber.getUpdatedAt());
    }

}
