package com.cms.dto.response;

import com.cms.model.ReactionBaseDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuoteReactionResponse {
    
    private String id;
    private UserResponse user;
    private ReactionBaseDocument.ReactionType type;
    private Boolean analysed;
    private String comment;
    private String quoteId;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
