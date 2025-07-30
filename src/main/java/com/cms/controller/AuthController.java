package com.cms.controller;

import com.cms.dto.request.LoginRequest;
import com.cms.dto.response.ApiResponse;
import com.cms.dto.response.JwtResponse;
import com.cms.model.User;
import com.cms.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(jwtResponse));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<JwtResponse.UserInfo>> getCurrentUser() {
        User user = authService.getCurrentUser();
        JwtResponse.UserInfo userInfo = new JwtResponse.UserInfo(user);
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }
}