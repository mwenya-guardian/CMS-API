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
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Setter
@Getter
@Document(collection = "publications")
//@RequiredArgsConstructor
public class Publication extends BaseDocument{
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

}