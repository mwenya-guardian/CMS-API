package com.cms.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "newsletter_schedules")
public class NewsletterSchedule extends BaseDocument {
    @Id
    private String id;

    private String title;
    private String description;

    // Cron expression, e.g. "0 0 9 ? * SUN" (cron format) — see docs below
    private String cronExpression;

    // timezone like "Africa/Lusaka"
    private String zoneId;

    // Which bulletins to include (ids)
    private List<String> bulletinIds;

    // send to all subscribers or targeted group
    private boolean sendToAll = true;
    private List<String> subscriberIds; // optional (if not sendToAll)

    private boolean enabled = true;

    private Instant lastRunAt;
    private Instant nextRunAt;
}
