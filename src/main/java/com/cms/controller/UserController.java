package com.cms.controller;

import com.cms.dto.request.UserRequest;
import com.cms.model.User;
import com.cms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import com.cms.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Create User
    @PostMapping
    public ResponseEntity<ApiResponse<User>>  createUser(@Valid @RequestBody UserRequest request) {
        User createdUser = userService.create(request);
        return ResponseEntity.ok(ApiResponse.success(createdUser));
    }

    // Get all users
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAll()));
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getById(id)));
    }

    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserRequest request
    ) {
        User updatedUser = userService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedUser));
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
