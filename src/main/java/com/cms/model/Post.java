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
    private String id;

    @NotNull
    private PostType type;

    private String caption;

    private String resourceUrl; //image or video

    // whether the media is public or requires auth (applies to media/text visibility)
    @NotNull
    private Boolean isPublic = Boolean.FALSE;

    public enum PostType{
        IMAGE, VIDEO, TEXT
    }
}
