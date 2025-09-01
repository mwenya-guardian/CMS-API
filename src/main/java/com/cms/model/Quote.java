package com.cms.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "quotes")
//@RequiredArgsConstructor
public class Quote extends BaseDocument{
    @Id
    private String id;
    
    @NotBlank(message = "Text is required")
    private String text;
    
    @NotBlank(message = "Author is required")
    private String author;
    
    private String source;
    private String category;
    private String imageUrl;
    private Boolean featured = false;


    //Constructor
    public Quote(String text, String author) {
        this.text = text;
        this.author = author;
    }
}