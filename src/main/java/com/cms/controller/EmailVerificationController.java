package com.cms.controller;

import com.cms.dto.response.ApiResponse;
import com.cms.model.EmailVerification;
import com.cms.service.EmailVerificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/verification")
@AllArgsConstructor
public class EmailVerificationController {
    
    private final EmailVerificationService verificationService;
    
    /**
     * Verify user registration code
     * POST /verification/verify-user
     */
    @PostMapping("/verify-user")
    public ResponseEntity<ApiResponse<String>> verifyUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        
        if (email == null || code == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email and verification code are required"));
        }
        
        try {
            boolean verified = verificationService.verifyUserCode(email, code);
            if (verified) {
                return ResponseEntity.ok(ApiResponse.success("Email verified successfully. You can now login."));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired verification code"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Verification failed: " + e.getMessage()));
        }
    }
    
    
    /**
     * Verify newsletter subscription code
     * POST /verification/verify-newsletter
     */
    @PostMapping("/verify-newsletter")
    public ResponseEntity<ApiResponse<String>> verifyNewsletter(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        
        if (email == null || code == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email and verification code are required"));
        }
        
        try {
            boolean verified = verificationService.verifyNewsletterCode(email, code);
            if (verified) {
                return ResponseEntity.ok(ApiResponse.success("Newsletter subscription verified successfully."));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired verification code"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Verification failed: " + e.getMessage()));
        }
    }
    
    /**
     * Resend verification code
     * POST /verification/resend
     */
    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<String>> resendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String type = request.get("type");
        
        if (email == null || type == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email and type are required"));
        }
        
        try {
            EmailVerification.VerificationType verificationType = 
                EmailVerification.VerificationType.valueOf(type.toUpperCase());
            
            verificationService.resendVerificationCode(email, verificationType);
            return ResponseEntity.ok(ApiResponse.success("Verification code sent successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to resend verification code: " + e.getMessage()));
        }
    }
}
