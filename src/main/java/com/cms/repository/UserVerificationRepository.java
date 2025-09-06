package com.cms.repository;

import com.cms.model.UserVerification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserVerificationRepository extends MongoRepository<UserVerification, String> {
    Optional<UserVerification> findByEmail(String email);
}
