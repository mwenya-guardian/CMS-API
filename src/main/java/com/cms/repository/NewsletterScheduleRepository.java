package com.cms.repository;
import com.cms.model.NewsletterSchedule;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NewsletterScheduleRepository extends MongoRepository<NewsletterSchedule, String> {}