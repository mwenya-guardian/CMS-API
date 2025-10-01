package com.cms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterScheduleResponse {
    
    private String id;
    private String title;
    private String description;
    private String cronExpression;
    private String zoneId;
    private List<String> bulletinIds;
    private Boolean sendToAll;
    private List<String> subscriberIds;
    private Boolean enabled;
    private Instant lastRunAt;
    private Instant nextRunAt;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
