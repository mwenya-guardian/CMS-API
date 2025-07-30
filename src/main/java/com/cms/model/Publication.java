package com.cms.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
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
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Setter
@Getter
@Document(collection = "publications")
public class Publication {
    @Id
    private String id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private String imageUrl;
    
    @NotNull(message = "Date is required")
    @Indexed
    private LocalDateTime date;
    
    @NotNull(message = "Layout type is required")
    private LayoutType layoutType;
    
    private String author;
    private List<String> tags;
    private Boolean featured = false;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @DBRef(lazy = true)
    @Field(name = "publication_reaction")
    private Set<PublicationReaction> reactions = new HashSet<>();

    public enum LayoutType {
        GRID("grid"), LIST("list"), MASONRY("masonry");
        private final String alt;

        LayoutType(String alt){
            this.alt = alt;
        }
    }

    
    public Publication(String title, String content, LocalDateTime date, LayoutType layoutType) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.layoutType = layoutType;
    }
    
//    // Getters and Setters
//    public String getId() { return id; }
//    public void setId(String id) { this.id = id; }
//
//    public String getTitle() { return title; }
//    public void setTitle(String title) { this.title = title; }
//
//    public String getContent() { return content; }
//    public void setContent(String content) { this.content = content; }
//
//    public String getImageUrl() { return imageUrl; }
//    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
//
//    public LocalDateTime getDate() { return date; }
//    public void setDate(LocalDateTime date) { this.date = date; }
//
//    public LayoutType getLayoutType() { return layoutType; }
//    public void setLayoutType(LayoutType layoutType) { this.layoutType = layoutType; }
//
//    public String getAuthor() { return author; }
//    public void setAuthor(String author) { this.author = author; }
//
//    public List<String> getTags() { return tags; }
//    public void setTags(List<String> tags) { this.tags = tags; }
//
//    public Boolean getFeatured() { return featured; }
//    public void setFeatured(Boolean featured) { this.featured = featured; }
//
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//
//    public LocalDateTime getUpdatedAt() { return updatedAt; }
//    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}