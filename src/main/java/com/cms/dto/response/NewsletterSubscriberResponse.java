package com.cms.dto.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
public class NewsletterSubscriberResponse {
    private String id;
    private String email;
    private Boolean active;
    private Boolean verified;
    private Instant createdAt;
    private Instant updatedAt;
}
