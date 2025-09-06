package com.cms.dto.response;

import com.cms.model.ReactionBaseDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReactionResponse {
    private String id;
    private String userId;
    private String targetId;
    private ReactionBaseDocument.ReactionType type;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;
}
