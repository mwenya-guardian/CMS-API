package com.cms.dto.request;

import com.cms.model.Event.EventCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
public class EventRequest {
    // Getters and Setters
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String imageUrl;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    private String location;
    
    @NotNull(message = "Category is required")
    private EventCategory category;
    
    private Boolean featured;

}