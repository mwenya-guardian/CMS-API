package com.cms.dto.request;

import jakarta.validation.constraints.NotBlank;

public class QuoteRequest {
    @NotBlank(message = "Text is required")
    private String text;
    
    @NotBlank(message = "Author is required")
    private String author;
    
    private String source;
    private String category;
    private String imageUrl;
    private Boolean featured;
    
    // Constructors
    public QuoteRequest() {}
    
    // Getters and Setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }
}