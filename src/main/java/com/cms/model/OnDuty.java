package com.cms.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
//@Document(collection = "OnDuty")
public class OnDuty {
//  e.g Deacon / pianist
    private String role;
//  Music / First Service
    private String activity;
    @NotNull
    private List<String> participates;
    @NotNull
    @Indexed
    private LocalDate date;

}
