package com.cms.dto.request;

import com.cms.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 2 and 100 characters")
    private String lastname;

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 2 and 100 characters")
    private String firstname;

    @Past
    private LocalDate dob;

    private User.UserRole role;
}
