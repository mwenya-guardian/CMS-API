package com.cms.controller;

import com.cms.dto.request.UserRequest;
import com.cms.dto.response.PageResponse;
import com.cms.dto.response.UserResponse;
import com.cms.model.User;
import com.cms.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.cms.dto.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>>  createUserPublic(@Valid @RequestBody UserRequest request) {
        User createdUser = userService.createPublic(request);
        return ResponseEntity.ok(ApiResponse.success(createdUser));
    }
    // Create User
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>>  createUser(@Valid @RequestBody UserRequest request) {
        User createdUser = userService.create(request);
        return ResponseEntity.ok(ApiResponse.success(createdUser));
    }

    // Get all users
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsersPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.getPaginated(page, limit)));
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getById(id)));
    }

    // Update user
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserRequest request
    ) {
        User updatedUser = userService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedUser));
    }

    // Delete user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
