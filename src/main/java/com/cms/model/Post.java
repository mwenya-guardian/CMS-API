package com.cms.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Post extends BaseDocument{
    @Id
    private String Id;

    @NotNull
    private PostType type;

    private String caption;

    private String resourceUrl; //image or video

    public enum PostType{
        MEDIA, TEXT
    }
}
