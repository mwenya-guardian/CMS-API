package com.cms.dto.request;

import com.cms.model.Announcement;
import com.cms.model.Cover;
import com.cms.model.Bulletin.PublicationStatus;
import com.cms.model.OnDuty;
import com.cms.model.Schedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulletinRequest {

    private String title;

    private LocalDate bulletinDate;

    private String content;

    private Cover cover;

    private PublicationStatus status;

    private Instant publishedAt;

    private Instant scheduledPublishAt;

    /**
     * id of the author user; stored as DBRef in Bulletin
     */
    private String authorId;

    /**
     * optional list of schedule ids to include
     */
    private Set<Schedule> schedules;

    /**
     * optional list of announcement ids to include
     */
    private Set<Announcement> announcements;

    /**
     * optional list of on-duty record ids to include
     */
    private Set<OnDuty> onDuty;
}
