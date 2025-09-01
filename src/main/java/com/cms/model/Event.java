package com.cms.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
//@RequiredArgsConstructor
@Document(collection = "events")
public class Event extends BaseDocument{
    @Id
    private String id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String imageUrl;

    @NotNull(message = "Start date is required")
    @Indexed
    private LocalDateTime startDate;

    private LocalDateTime endDate;
    private String location;

    @NotNull(message = "Category is required")
    private EventCategory category;

    private Boolean featured = false;

    @DBRef
    @Field(name = "event_reaction")
    private Set<EventReaction> reactions = new HashSet<>();

    public enum EventCategory {
        WEDDING, CONFERENCE, WORKSHOP, SOCIAL, OTHER
    }

    // Constructors
    public Event(String title, String description, LocalDateTime startDate, EventCategory category) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.category = category;
    }

}