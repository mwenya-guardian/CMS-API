package com.cms.dto.response;

import com.cms.model.User;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(String email, String firstname, String lastname, LocalDate dob, User.UserRole role, LocalDateTime lastLogin) {
}
