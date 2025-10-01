package com.cms.dto.response;

import com.cms.model.Publication;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublicationResponse {
    
    private String id;
    private String title;
    private String content;
    private String imageUrl;
    private LocalDateTime date;
    private Publication.LayoutType layoutType;
    private String author;
    private List<String> tags;
    private Boolean featured;
    private Set<PublicationReactionResponse> reactions;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
