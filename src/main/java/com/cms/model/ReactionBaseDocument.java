package com.cms.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class ReactionBaseDocument extends BaseDocument{
    @Id
    private String id;

    @DBRef(lazy = true)
    @Indexed
    private User user;

    private ReactionType type;

    public enum ReactionType{
        LIKE, DISLIKE, COMMENT
    }
}

