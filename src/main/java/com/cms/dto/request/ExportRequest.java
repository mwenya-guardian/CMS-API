package com.cms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ExportRequest {
    @NotBlank(message = "Format is required")
    private String format;
    
    @NotEmpty(message = "Items list cannot be empty")
    private List<String> items;
    
    private String title;
    private Boolean includeImages = true;
    private String pageSize = "A4";
    private String orientation = "portrait";
    
    // Constructors
    public ExportRequest() {}
    
    // Getters and Setters
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    
    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public Boolean getIncludeImages() { return includeImages; }
    public void setIncludeImages(Boolean includeImages) { this.includeImages = includeImages; }
    
    public String getPageSize() { return pageSize; }
    public void setPageSize(String pageSize) { this.pageSize = pageSize; }
    
    public String getOrientation() { return orientation; }
    public void setOrientation(String orientation) { this.orientation = orientation; }
}