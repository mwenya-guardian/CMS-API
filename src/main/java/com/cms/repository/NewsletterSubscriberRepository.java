package com.cms.repository;

import com.cms.model.NewsletterSubscriber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NewsletterSubscriberRepository extends MongoRepository<NewsletterSubscriber, String> {
    List<NewsletterSubscriber> findByActiveTrue(Sort sort);
    List<NewsletterSubscriber> findByActiveTrueAndVerifiedTrue();
    Page<NewsletterSubscriber> findByActiveTrueAndVerifiedTrue(Pageable page);
    Optional<NewsletterSubscriber> findByEmail(String email);
    boolean existsByEmail(String email);
}
