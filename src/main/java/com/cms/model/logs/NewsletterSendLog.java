package com.cms.model.logs;

import com.cms.model.BaseDocument;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "newsletter_logs")
public class NewsletterSendLog extends BaseDocument {
    @Id
    private String id;
    private String scheduleId;
    private Instant sentAt;
    private int successCount;
    private int failureCount;
    private String details; // JSON or text summary
}
