package com.cms.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "analysis_schedules")
public class AnalysisSchedule extends BaseDocument {
    
    @Id
    private String id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Cron expression is required")
    private String cronExpression;
    
    @NotBlank(message = "Zone ID is required")
    private String zoneId;
    
    @NotNull(message = "Model type is required")
    @Indexed
    private ReactionTrackedModel.ModelType modelType;
    
    private boolean enabled = true;
    
    private Instant lastRunAt;
    
    private String description;
    
    public AnalysisSchedule(String title, String cronExpression, String zoneId, ReactionTrackedModel.ModelType modelType) {
        this.title = title;
        this.cronExpression = cronExpression;
        this.zoneId = zoneId;
        this.modelType = modelType;
        this.enabled = true;
    }
    
    public AnalysisSchedule(String title, String cronExpression, String zoneId, ReactionTrackedModel.ModelType modelType, String description) {
        this.title = title;
        this.cronExpression = cronExpression;
        this.zoneId = zoneId;
        this.modelType = modelType;
        this.description = description;
        this.enabled = true;
    }
}
