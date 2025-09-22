package com.cms.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "email_verifications")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerification {
    
    @Id
    private String id;
    
    private String email;
    
    private VerificationType type;
    
    private String verificationCode;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime expiresAt;
    
    private boolean used;
    
    public enum VerificationType {
        USER_REGISTRATION,
        NEWSLETTER_SUBSCRIPTION
    }
    
    public EmailVerification(String email, VerificationType type, String verificationCode) {
        this.email = email;
        this.type = type;
        this.verificationCode = verificationCode;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(15); // 15 minutes expiry
        this.used = false;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isValid() {
        return !used && !isExpired();
    }
}
