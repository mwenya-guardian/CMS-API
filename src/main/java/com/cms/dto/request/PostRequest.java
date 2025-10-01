package com.cms.dto.request;

import com.cms.model.Post;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {
    
    @NotNull(message = "Post type is required")
    private Post.PostType type;

    private String caption;

    private String resourceUrl;

    @NotNull(message = "Public visibility setting is required")
    private Boolean isPublic;
}
