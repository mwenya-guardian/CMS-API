package com.cms.dto.request;

import com.cms.service.ReactionService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
public class BulkAnalysisRequest {
    private ReactionService.ReactionCategory entityType;
    private String entityId;
    private String[] modes; // e.g. ["sentiment","tone","moderation"]
    private Instant since;  // optional - limit comments to analyze
    private int maxComments = 1000;

    public ReactionService.ReactionCategory getEntityType() {
        return this.entityType;
    }

    public String getEntityId() {
        return this.entityId;
    }

    public String[] getModes() {
        return this.modes;
    }

    public Instant getSince() {
        return this.since;
    }

    public int getMaxComments() {
        return this.maxComments;
    }

    public void setEntityType(ReactionService.ReactionCategory entityType) {
        this.entityType = entityType;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setModes(String[] modes) {
        this.modes = modes;
    }

    public void setSince(Instant since) {
        this.since = since;
    }

    public void setMaxComments(int maxComments) {
        this.maxComments = maxComments;
    }
}
