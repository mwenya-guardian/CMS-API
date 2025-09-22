package com.cms.service;

import com.cms.model.EmailVerification;
import com.cms.model.EmailVerification.VerificationType;
import com.cms.repository.EmailVerificationRepository;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@AllArgsConstructor
public class EmailVerificationService {
    
    private final EmailVerificationRepository verificationRepository;
    private final GmailSmtpEmailService emailService;
    
    /**
     * Generate and send verification code for user registration
     */
    public void sendUserVerificationCode(String email) throws MessagingException, IOException {
        String normalizedEmail = email.trim().toLowerCase();
        cleanupExpiredCodes();
        EmailVerification existing = verificationRepository
            .findByEmailAndType(normalizedEmail, VerificationType.USER_REGISTRATION).orElse(null);
        if (existing != null) {
            // Remove any existing record to issue a fresh code
            verificationRepository.delete(existing);
        }
        // Generate new verification code
        String code = generateVerificationCode();
        EmailVerification verification = new EmailVerification(normalizedEmail, EmailVerification.VerificationType.USER_REGISTRATION, code);
        verificationRepository.save(verification);
        
        // Send email
        String subject = "SDA Church - Account Verification";
        String message = String.format(
            "Welcome to SDA Church!\n\n" +
            "Your verification code is: %s\n\n" +
            "This code will expire in 15 minutes.\n\n" +
            "If you did not request this verification, please ignore this email.",
            code
        );
        
        emailService.sendPlain(normalizedEmail, subject, message);
    }
    
    /**
     * Generate and send verification code for newsletter subscription
     */
    public void sendNewsletterVerificationCode(String email) throws MessagingException, IOException {
        String normalizedEmail = email.trim().toLowerCase();
        cleanupExpiredCodes();
        EmailVerification existing = verificationRepository
            .findByEmailAndType(normalizedEmail, VerificationType.NEWSLETTER_SUBSCRIPTION).orElse(null);
        if (existing != null) {
            // Remove any existing record to issue a fresh code
            verificationRepository.delete(existing);
        }
        // Generate new verification code
        String code = generateVerificationCode();
        EmailVerification verification = new EmailVerification(normalizedEmail, VerificationType.NEWSLETTER_SUBSCRIPTION, code);
        verificationRepository.save(verification);
        
        // Send email
        String subject = "SDA Church - Newsletter Subscription Verification";
        String message = String.format(
            "Thank you for subscribing to our newsletter!\n\n" +
            "Your verification code is: %s\n\n" +
            "This code will expire in 15 minutes.\n\n" +
            "If you did not request this subscription, please ignore this email.",
            code
        );
        
        emailService.sendPlain(normalizedEmail, subject, message);
    }
    
    /**
     * Verify user registration code and activate user
     */
    @Transactional
    public boolean verifyUserCode(String email, String code) {
        String normalizedEmail = email.trim().toLowerCase();
        cleanupExpiredCodes();
        
        EmailVerification verification = verificationRepository
            .findByEmailAndVerificationCodeAndType(normalizedEmail, code, VerificationType.USER_REGISTRATION)
            .orElse(null);
        
        if (verification == null || !verification.isValid()) {
            return false;
        } else {
            verificationRepository.delete(verification);
            return true;
        }
    }
    
    /**
     * Verify newsletter subscription code and activate subscriber
     */
    @Transactional
    public boolean verifyNewsletterCode(String email, String code) {
        String normalizedEmail = email.trim().toLowerCase();
        cleanupExpiredCodes();
        
        EmailVerification verification = verificationRepository
            .findByEmailAndVerificationCodeAndType(normalizedEmail, code, VerificationType.NEWSLETTER_SUBSCRIPTION)
            .orElse(null);
        
        if (verification == null || !verification.isValid()) {
            return false;
        }
        verificationRepository.delete(verification);
        return true;
    }
    
    /**
     * Resend verification code
     */
    public void resendVerificationCode(String email, VerificationType type) throws MessagingException, IOException {
        if (type == EmailVerification.VerificationType.USER_REGISTRATION) {
            sendUserVerificationCode(email);
        } else {
            sendNewsletterVerificationCode(email);
        }
    }
    
    /**
     * Clean up expired verification codes
     */
    public void cleanupExpiredCodes() {
        verificationRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
    
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}
