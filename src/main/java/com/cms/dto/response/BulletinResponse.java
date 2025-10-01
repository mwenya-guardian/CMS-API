package com.cms.dto.response;

import com.cms.model.Bulletin;
import com.cms.model.Cover;
import com.cms.model.Schedule;
import com.cms.model.Announcement;
import com.cms.model.OnDuty;
import com.cms.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulletinResponse {
    
    private String id;
    private String title;
    private Cover cover;
    private LocalDate bulletinDate;
    private String content;
    private Bulletin.PublicationStatus status;
    private Date scheduledPublishAt;
    private User author;
    private Set<Schedule> schedules;
    private Set<Announcement> announcements;
    private Set<OnDuty> onDutyList;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
