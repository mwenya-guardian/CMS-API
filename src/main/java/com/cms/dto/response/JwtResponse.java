package com.cms.dto.response;

import com.cms.model.User;

import java.time.LocalDateTime;

public class JwtResponse {
    private UserInfo user;
    private String token;
    private LocalDateTime expiresAt;
    
    // Constructors
    public JwtResponse() {}
    
    public JwtResponse(User user, String token, LocalDateTime expiresAt) {
        this.user = new UserInfo(user);
        this.token = token;
        this.expiresAt = expiresAt;
    }
    
    // Inner class for user info
    public static class UserInfo {
        private String id;
        private String email;
        private String name;
        private String role;
        private String avatar;
        private LocalDateTime createdAt;
        private LocalDateTime lastLogin;
        
        public UserInfo(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.name = user.getName();
            this.role = user.getRole().name().toLowerCase();
            this.avatar = user.getAvatar();
            this.createdAt = user.getCreatedAt();
            this.lastLogin = user.getLastLogin();
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getLastLogin() { return lastLogin; }
        public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    }
    
    // Getters and Setters
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}