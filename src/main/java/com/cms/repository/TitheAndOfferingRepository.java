package com.cms.repository;

import com.cms.model.TitheAndOffering;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TitheAndOfferingRepository extends MongoRepository<TitheAndOffering, String> {
}
