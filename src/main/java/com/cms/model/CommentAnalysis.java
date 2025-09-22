package com.cms.model;

import com.cms.service.ReactionService;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "comment_analysis")
public class CommentAnalysis {
    @Id
    private String id;

    // reference to existing comment
    private String commentId;
    private ReactionService.ReactionCategory entityType; // POST, EVENT, QUOTE, PUBLICATION
    private String entityId;

    // analysis results
    private String sentiment;        // positive|neutral|negative
    private Double sentimentScore;   // 0.0 - 1.0
    private String tone;             // e.g. excited, sarcastic, concerned
    private Double toneConfidence;   // 0.0 - 1.0

    private Boolean moderationFlagged;
    private List<String> moderationCategories;
    private Double moderationConfidence;

    // metadata
    private String modelName;
    private String modelVersion;
    private Instant analyzedAt;
    private String rawResponse; // raw JSON from LLM (for debugging)
}
