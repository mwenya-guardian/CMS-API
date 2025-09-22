package com.cms.model;

import com.cms.service.ReactionService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;


@NoArgsConstructor
@AllArgsConstructor
//@Builder
@Document(collection = "analysis_jobs")
public class AnalysisJob {
    @Id
    private String id;
    private ReactionService.ReactionCategory entityType;
    private String entityId;
    private Instant submittedAt;
    private AnalysisStatus status; // PENDING, RUNNING, PARTIAL, SUCCESS, FAILED
    private int totalComments;
    private int processedComments;
    private Map<String, Object> meta; // e.g. chunk size, model name
    private String failureReason;
    private Instant completedAt;

    public void setId(String id) {
        this.id = id;
    }

    public void setEntityType(ReactionService.ReactionCategory entityType) {
        this.entityType = entityType;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public void setStatus(AnalysisStatus status) {
        this.status = status;
    }

    public void setTotalComments(int totalComments) {
        this.totalComments = totalComments;
    }

    public void setProcessedComments(int processedComments) {
        this.processedComments = processedComments;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getId() {
        return this.id;
    }

    public ReactionService.ReactionCategory getEntityType() {
        return this.entityType;
    }

    public String getEntityId() {
        return this.entityId;
    }

    public Instant getSubmittedAt() {
        return this.submittedAt;
    }

    public AnalysisStatus getStatus() {
        return this.status;
    }

    public int getTotalComments() {
        return this.totalComments;
    }

    public int getProcessedComments() {
        return this.processedComments;
    }

    public Map<String, Object> getMeta() {
        return this.meta;
    }

    public String getFailureReason() {
        return this.failureReason;
    }

    public Instant getCompletedAt() {
        return this.completedAt;
    }

    public enum AnalysisStatus {
        PENDING, RUNNING, PARTIAL, SUCCESS, FAILED
    }
}
