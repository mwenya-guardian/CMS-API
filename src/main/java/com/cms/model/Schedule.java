package com.cms.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import com.cms.model.OnDuty;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
// @Document(collection = "Schedules")
public class Schedule {
    @NotBlank
    private String title;
    @NotNull
    private HashMap<String, String> scheduledActivities;
    @NotNull
    private LocalTime startTime;
    @NotNull
    private LocalTime endTime;
    @NotNull
    private LocalDate scheduledDate;
}
