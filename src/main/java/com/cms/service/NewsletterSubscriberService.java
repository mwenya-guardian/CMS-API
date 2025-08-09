package com.cms.service;

import com.cms.model.NewsletterSubscriber;
import com.cms.repository.NewsletterSubscriberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class NewsletterSubscriberService {

    private final NewsletterSubscriberRepository repo;

    /**
     * fetch all active subscribers sorted by email
     */
    public List<NewsletterSubscriber> getAllActive() {
        return repo.findByActiveTrue(Sort.by(Sort.Direction.ASC, "email"));
    }

    /**
     * fetch every subscriber regardless of active flag
     */
    public List<NewsletterSubscriber> getAll() {
        return repo.findAll(Sort.by(Sort.Direction.ASC, "email"));
    }

    /**
     * find a subscriber by id
     */
    public Optional<NewsletterSubscriber> getById(String id) {
        return repo.findById(id);
    }

    /**
     * find a subscriber by email
     */
    public Optional<NewsletterSubscriber> getByEmail(String email) {
        return repo.findByEmail(email);
    }

    /**
     * add a new subscriber
     */
    public NewsletterSubscriber create(NewsletterSubscriber sub) {
        // you might generate a verificationToken here before saving
        return repo.save(sub);
    }

    /**
     * update an existing subscriber; throws if not found
     */
    public NewsletterSubscriber update(String id, NewsletterSubscriber incoming) {
        NewsletterSubscriber existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));
        existing.setEmail(incoming.getEmail());
        existing.setActive(incoming.getActive());
        existing.setVerificationToken(incoming.getVerificationToken());
        existing.setVerified(incoming.getVerified());
        return repo.save(existing);
    }

    /**
     * delete a subscriber by id; throws if not found
     */
    public void delete(String id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Subscriber not found");
        }
        repo.deleteById(id);
    }
}
