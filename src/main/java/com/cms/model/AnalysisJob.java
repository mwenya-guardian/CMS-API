package com.cms.model;

import com.cms.service.ReactionService;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    public enum AnalysisStatus {
        PENDING, RUNNING, PARTIAL, SUCCESS, FAILED
    }
}
