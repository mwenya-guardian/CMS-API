package com.cms.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "newsletter_subscribers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NewsletterSubscriber extends BaseDocument {

    @Id
    private String id;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    private Boolean active = true;

    private String verificationToken;

    private Boolean verified = false;
}
