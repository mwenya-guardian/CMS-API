package com.cms.repository;

import com.cms.model.EmailVerification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends MongoRepository<EmailVerification, String> {
    
    Optional<EmailVerification> findByEmailAndTypeAndUsedFalse(String email, EmailVerification.VerificationType type);
    
    Optional<EmailVerification> findByEmailAndVerificationCodeAndType(String email, String code, EmailVerification.VerificationType type);
    
    Optional<EmailVerification> findByEmailAndType(String email, EmailVerification.VerificationType type);
    
    void deleteByEmailAndType(String email, EmailVerification.VerificationType type);
    
    void deleteByCreatedAtBefore(LocalDateTime dateTime);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
