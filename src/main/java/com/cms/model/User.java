package com.cms.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Document(collection = "users")
public class User extends BaseDocument{
    // Getters and Setters
    @Id
    private String id;
    

    @Email(message = "Email should be valid")
    @Indexed(unique = true)
    private String email;
    

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String firstname;


    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String lastname;
    

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    private UserRole role = UserRole.VIEWER;
//    private String avatar;

    private LocalDate dob;
    private LocalDateTime lastLogin;
    
    public enum UserRole {
        ADMIN, EDITOR, USER, VIEWER
    }

    public User(String id) {
        this.id = id;
    }
    
    public User(String email, String firstname, String lastname, String password, LocalDate dob, UserRole role) {
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
        this.dob = dob;
        this.role = role;
    }

}