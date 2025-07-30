package com.cms.repository;

import com.cms.model.Bulletin;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BulletinRepository extends MongoRepository<Bulletin, String> {
    // define custom finder methods if needed, e.g.
    // List<BulletinDocument> findByStatus(PublicationStatus status);
}
