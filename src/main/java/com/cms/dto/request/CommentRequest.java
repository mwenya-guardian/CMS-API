package com.cms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

public record CommentRequest (
    String id,
    String content,
    Instant createdAt
){};
