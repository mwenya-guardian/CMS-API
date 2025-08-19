package com.cms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class ExportRequest {
    @NotBlank(message = "Format is required")
    private String format;
    
    @NotEmpty(message = "Items list cannot be empty")
    private List<String> items;
    
    private String title;
    private Boolean includeImages = true;
    private String pageSize = "A4";
    private String orientation = "portrait";
}