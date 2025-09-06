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
public class UpdateReactionRequest {
    private ReactionBaseDocument.ReactionType type;
    private String comment;
}
