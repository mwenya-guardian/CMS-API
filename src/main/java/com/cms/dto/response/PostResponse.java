package com.cms.dto.response;

import com.cms.model.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    
    private String id;
    private Post.PostType type;
    private String caption;
    private String resourceUrl;
    private Boolean isPublic;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
