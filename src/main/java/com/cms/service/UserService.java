package com.cms.service;

import com.cms.dto.request.UserRequest;
import com.cms.dto.request.UserUpdateRequest;
import com.cms.dto.response.PageResponse;
import com.cms.dto.response.UserResponse;
import com.cms.exception.BadRequestException;
import com.cms.exception.DuplicateResourceException;
import com.cms.exception.ResourceNotFoundException;
import com.cms.model.User;
import com.cms.repository.UserRepository;

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
import java.util.Random;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;


    
    public List<User> getAll() {
        return userRepository.findAll();
    }

    
    public PageResponse<UserResponse> getPaginated(int page, int size) {
        // controller used page default = 1; convert to 0-based page index here (caller may choose differently)
        int p = Math.max(0, page - 1);

        Page<User> userPage = userRepository.findAll(PageRequest.of(p, size));
        return new PageResponse<>(userPage.getContent().stream().map(this::mapUserToUserResponse).toList(), userPage.getNumber(), userPage.getSize(), userPage.getTotalPages());
    }

    
    public User getById(String id) {
        return userRepository.findById(id).orElseThrow( ()->
                new ResourceNotFoundException("User not found")
        );
    }

    
    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow( ()->
                new ResourceNotFoundException("User not found")
        );
    }

    
    @Transactional
    public User create(UserRequest request) {
        // basic validation (controller layer already validates jakarta constraints)
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
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
        return userRepository.save(u);
    }

    @Transactional
    public User createPublic(UserRequest request) throws MessagingException, IOException{
        // basic validation (controller layer already validates jakarta constraints)
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        User userExisting = userRepository.findByEmail(request.getEmail().trim().toLowerCase()).orElse(null);
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

        User savedUser = userRepository.save(u);
        emailVerificationService.sendUserVerificationCode(u.getEmail());
        // createdAt and createdBy handled by BaseDocument / auditing annotations
        return savedUser;
    }
    
    
    @Transactional
    public User update(String id, UserUpdateRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        // If updating email ensure uniqueness
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(existing.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already in use");
            }
            existing.setEmail(request.getEmail().trim().toLowerCase());
        }

        if (request.getFirstname() != null) existing.setFirstname(request.getFirstname());
        if (request.getLastname() != null) existing.setLastname(request.getLastname());
        if (request.getDob() != null) existing.setDob(request.getDob());
        if (request.getRole() != null) existing.setRole(request.getRole());

        // If password present, encode and update; otherwise keep existing
        // if (request.getPassword() != null && !request.getPassword().isBlank()) {
        //     existing.setPassword(passwordEncoder.encode(request.getPassword()));
        // }

        return userRepository.save(existing);
    }

    
    @Transactional
    public void delete(String id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        userRepository.delete(u);
    }

    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    
    @Transactional
    public User changePassword(String email, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        u.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(u);
    }
    @Transactional
    public Boolean resetPassword(String email) throws MessagingException, IOException {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        String newPassword = generateRandomPassword();
        u.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(u);
        emailService.sendPlain(email, "Reset Password", "Your new password is: " + newPassword);
        return true;
    }


    
    @Transactional
    public void updateLastLogin(String id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setLastLogin(LocalDateTime.now());
            userRepository.save(u);
        });
    }
    private UserResponse mapUserToUserResponse(User user){
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstname(), user.getLastname(), user.getDob(),user.getRole(), user.getLastLogin());
    }

    @Transactional
    public boolean verifyUser(String email, String code){
        String normalizedEmail = email.trim().toLowerCase();
        User existing = userRepository.findByEmail(normalizedEmail).orElse(null);
        if(existing == null){
            throw new ResourceNotFoundException("Email is not registered as a user");
        }
        if(emailVerificationService.verifyUserCode(normalizedEmail, code)){
            existing.setActive(true);
            userRepository.save(existing);
            return true;
        }
        return false;

    }

    public String getFullName(String id) {
        User user = getById(id);
        return user.getFirstname() + " " + user.getLastname();
    }

    /**
     * Generates a random password string of at least 6 characters
     * containing uppercase letters, lowercase letters, and numbers
     */
    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        
        // Generate a password between 7-12 characters for better security
        int length = 7 + random.nextInt(6); // 7-12 characters
        
        for (int i = 0; i < length; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        return password.toString();
    }
}
