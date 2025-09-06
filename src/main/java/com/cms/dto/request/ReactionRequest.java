package com.cms.dto.request;

import com.cms.model.ReactionBaseDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRequest {
    /**
     * targetId - id of the target entity (postId/eventId/...). optional for some operations
     */
    private String targetId;

    /**
     * Reaction type: LIKE, DISLIKE, COMMENT
     */
    private ReactionBaseDocument.ReactionType type;

    /**
     * Optional comment text (only used when type == COMMENT)
     */
    private String comment;
}
