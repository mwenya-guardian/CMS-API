package com.cms.dto.response;

import com.cms.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.CloseableThreadContext;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
public class JwtResponse {
    // Getters and Setters
    private UserInfo user;
    private String token;
    private LocalDateTime expiresAt;
    
    public JwtResponse(User user, String token, LocalDateTime expiresAt) {
        this.user = new UserInfo(user);
        this.token = token;
        this.expiresAt = expiresAt;
    }
    
    // Inner class for user info
    @Getter
    @Setter
    public static class UserInfo {
        private String id;
        private String email;
        private String firstname;
        private String lastname;
        private String role;
        private LocalDate dob;
        private Instant createdAt;
        private LocalDateTime lastLogin;
        
        public UserInfo(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.firstname = user.getFirstname();
            this.lastname  = user.getLastname();
            this.role = user.getRole().name().toLowerCase();
            this.dob = user.getDob();
            this.createdAt = user.getCreatedAt();
            this.lastLogin = user.getLastLogin();
        }

    }

}