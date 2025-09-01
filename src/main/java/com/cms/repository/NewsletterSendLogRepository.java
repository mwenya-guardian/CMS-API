package com.cms.repository;

import com.cms.model.logs.NewsletterSendLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NewsletterSendLogRepository extends MongoRepository<NewsletterSendLog, String> {
}
