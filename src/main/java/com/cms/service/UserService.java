package com.cms.service;

import com.cms.dto.request.UserRequest;
import com.cms.dto.response.PageResponse;
import com.cms.dto.response.UserResponse;
import com.cms.exception.BadRequestException;
import com.cms.exception.DuplicateResourceException;
import com.cms.exception.ResourceNotFoundException;
import com.cms.model.User;
import com.cms.repository.UserRepository;

import io.jsonwebtoken.lang.Assert;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;


    
    public List<User> getAll() {
        return repository.findAll();
    }

    
    public PageResponse<UserResponse> getPaginated(int page, int size) {
        // controller used page default = 1; convert to 0-based page index here (caller may choose differently)
        int p = Math.max(0, page - 1);

        Page<User> userPage = repository.findAll(PageRequest.of(p, size));
        return new PageResponse<>(userPage.getContent().stream().map(this::mapUserToUserResponse).toList(), userPage.getNumber(), userPage.getSize(), userPage.getTotalPages());
    }

    
    public User getById(String id) {
        return repository.findById(id).orElseThrow( ()->
                new ResourceNotFoundException("User not found")
        );
    }

    
    public User getByEmail(String email) {
        return repository.findByEmail(email).orElseThrow( ()->
                new ResourceNotFoundException("User not found")
        );
    }

    
    @Transactional
    public User create(UserRequest request) {
        // basic validation (controller layer already validates jakarta constraints)
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use");
        }

        User u = new User();
        u.setEmail(request.getEmail().trim().toLowerCase());
        u.setFirstname(request.getFirstname());
        u.setLastname(request.getLastname());
        // encode provided password
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        u.setDob(request.getDob());
        u.setRole(request.getRole() == null ? User.UserRole.VIEWER : request.getRole());

        // createdAt and createdBy handled by BaseDocument / auditing annotations
        return repository.save(u);
    }

    @Transactional
    public User createPublic(UserRequest request) throws MessagingException, IOException{
        // basic validation (controller layer already validates jakarta constraints)
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        User userExisting = repository.findByEmail(request.getEmail().trim().toLowerCase()).orElse(null);
        if (userExisting  != null) {
            if(!userExisting.getActive()){
                emailVerificationService.sendUserVerificationCode(userExisting.getEmail());
                throw new DuplicateResourceException("Email already exists, but inactive");
            }
            throw new DuplicateResourceException("Email already in use");
            // return userExisting;
        }

        User u = new User();
        u.setEmail(request.getEmail().trim().toLowerCase());
        u.setFirstname(request.getFirstname());
        u.setLastname(request.getLastname());
        // encode provided password
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        u.setDob(request.getDob());
        u.setRole(User.UserRole.USER);
        u.setActive(false); // User starts as inactive until email verification

        User savedUser = repository.save(u);
        emailVerificationService.sendUserVerificationCode(u.getEmail());
        // createdAt and createdBy handled by BaseDocument / auditing annotations
        return savedUser;
    }
    
    
    @Transactional
    public User update(String id, UserRequest request) {
        User existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        // If updating email ensure uniqueness
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(existing.getEmail())) {
            if (repository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already in use");
            }
            existing.setEmail(request.getEmail().trim().toLowerCase());
        }

        if (request.getFirstname() != null) existing.setFirstname(request.getFirstname());
        if (request.getLastname() != null) existing.setLastname(request.getLastname());
        if (request.getDob() != null) existing.setDob(request.getDob());
        if (request.getRole() != null) existing.setRole(request.getRole());

        // If password present, encode and update; otherwise keep existing
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return repository.save(existing);
    }

    
    @Transactional
    public void delete(String id) {
        User u = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        repository.delete(u);
    }

    
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    
    @Transactional
    public User changePassword(String id, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }
        User u = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        u.setPassword(passwordEncoder.encode(newPassword));
        return repository.save(u);
    }


    
    @Transactional
    public void updateLastLogin(String id) {
        repository.findById(id).ifPresent(u -> {
            u.setLastLogin(LocalDateTime.now());
            repository.save(u);
        });
    }
    private UserResponse mapUserToUserResponse(User user){
        return new UserResponse(user.getEmail(), user.getFirstname(), user.getLastname(), user.getDob(),user.getRole());
    }

    @Transactional
    public boolean verifyUser(String email, String code){
        String normalizedEmail = email.trim().toLowerCase();
        User existing = repository.findByEmail(normalizedEmail).orElse(null);
        if(existing == null){
            throw new ResourceNotFoundException("Email is not registered as a user");
        }
        if(emailVerificationService.verifyUserCode(normalizedEmail, code)){
            existing.setActive(true);
            repository.save(existing);
            return true;
        }
        return false;

    }
}
