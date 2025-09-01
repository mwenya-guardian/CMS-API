package com.cms.model;

//import com.cms.enums.PublicationStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Document(collection = "bulletins")
public class Bulletin extends BaseDocument{

    @Id
    private String id;

    @Field("title")
    private String title;

    @Field("cover")
    private Cover cover;

    // you can keep LocalDate if you have a custom converter; else use Instant or Date
    @Field("bulletin_date")
    private LocalDate bulletinDate;

    @Field("content")
    private String content;

    @Field("status")
    private PublicationStatus status;

    @Field("scheduled_publish_at")
    private Date scheduledPublishAt;

    // reference to a user document
    @DBRef(lazy = true)
    @Field("author")
    private User author;

//    @DBRef(lazy = true)
    @Field("schedules")
    private Set<Schedule> schedules = new HashSet<>();

//    @DBRef(lazy = true)
    @Field("announcements")
    private Set<Announcement> announcements = new HashSet<>();

//    @DBRef(lazy = true)
    @Field("on_duty_list")
    private Set<OnDuty> onDutyList = new HashSet<>();

    public enum PublicationStatus{
        DRAFT, PUBLISHED, SCHEDULED
    }

}
