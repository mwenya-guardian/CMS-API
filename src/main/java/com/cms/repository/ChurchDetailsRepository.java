package com.cms.repository;

import com.cms.model.ChurchDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChurchDetailsRepository extends MongoRepository<ChurchDetails, String> {
}
