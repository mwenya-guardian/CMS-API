package com.cms.repository;

import com.cms.model.Bulletin;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BulletinRepository extends MongoRepository<Bulletin, String> {
    // define custom finder methods if needed, e.g.
     List<Bulletin> findByStatus(Bulletin.PublicationStatus status);
}
