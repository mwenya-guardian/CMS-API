package com.cms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterScheduleRequest {
    
    private String title;
    
    private String description;

    @NotBlank(message = "Cron expression is required")
    private String cronExpression;

    @NotBlank(message = "Timezone is required")
    private String zoneId;

    private List<String> bulletinIds;

    @NotNull(message = "Send to all setting is required")
    private Boolean sendToAll;

    private List<String> subscriberIds;

    @NotNull(message = "Enabled setting is required")
    private Boolean enabled;
}
