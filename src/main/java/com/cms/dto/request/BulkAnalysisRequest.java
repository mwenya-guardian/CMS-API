package com.cms.dto.request;

import com.cms.service.ReactionService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BulkAnalysisRequest {
    private ReactionService.ReactionCategory entityType;
    private String entityId;
    private String[] modes; // e.g. ["sentiment","tone","moderation"]
    private Instant since;  // optional - limit comments to analyze
    private int maxComments = 1000;

}
