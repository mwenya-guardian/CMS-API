package com.cms.dto.request;

import com.cms.model.*;
import com.cms.model.Bulletin.PublicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
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

    private Date scheduledPublishAt;

    private Set<Schedule> schedules;

    private Set<Announcement> announcements;

    private Set<OnDuty> onDutyList;
}
