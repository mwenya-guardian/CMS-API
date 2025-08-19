package com.cms.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cover {
    @NotBlank(message = "Document name required")
    private String documentName;
    @NotBlank(message = "Greeting message required")
    private String welcomeMessage;
    private String footerMessage;
//    private LocalDate datePublished;
}
