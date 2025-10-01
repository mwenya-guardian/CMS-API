package com.cms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {
    
    private String id;
    private String text;
    private String author;
    private String source;
    private String category;
    private String imageUrl;
    private Boolean featured;
    private Set<QuoteReactionResponse> reactions;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
