package com.cms.service;

import com.cms.dto.request.LoginRequest;
import com.cms.dto.response.JwtResponse;
import com.cms.model.User;
import com.cms.repository.UserRepository;
import com.cms.security.JwtTokenProvider;
import com.cms.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@AllArgsConstructor
public class AuthService {
    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider tokenProvider;
    
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        LocalDateTime expiresAt = tokenProvider.getExpirationDateFromToken(jwt)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        
        return new JwtResponse(user, jwt, expiresAt);
    }
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        throw new RuntimeException("No authenticated user found");
    }
    
    public void createDefaultAdmin() {
        if (!userRepository.existsByEmail("admin@cms.com")) {
            User admin = new User();
            admin.setEmail("admin@cms.com");
            admin.setName("Admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.UserRole.ADMIN);
            userRepository.save(admin);
        }
    }
}