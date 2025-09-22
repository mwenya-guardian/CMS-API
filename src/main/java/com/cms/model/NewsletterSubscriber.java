package com.cms.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "newsletter_subscribers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterSubscriber extends BaseDocument {

    @Id
    private String id;

    @NotBlank
    @Email
    @Indexed(unique = true)
    private String email;

    @NotNull
    private Boolean active = true;

    private String verificationToken;
    @NotNull
    private Boolean verified = false;
}
