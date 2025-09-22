package com.cms.model;

import com.cms.service.ReactionService;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "analysis_alerts")
public class AnalysisAlert {
    @Id
    private String id;
    private ReactionService.ReactionCategory entityType;
    private String entityId;
    private String metric; // e.g. negative_fraction
    private double currentValue;
    private double baselineValue;
    private Instant windowStart;
    private Instant windowEnd;
    private String severity; // INFO, WARNING, CRITICAL
    private String note;
    private Instant createdAt;
    private Map<String, Object> extra;
}
