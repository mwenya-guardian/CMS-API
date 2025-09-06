package com.cms.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

@NoArgsConstructor
@Getter
public abstract class ReactionBaseDocument extends BaseDocument{
    @Id
    @Setter
    private String id;

    @DBRef(lazy = true)
    @Indexed
    @Setter
    private User user;

    @Setter
    private ReactionType type;

    private String comment;

    public ReactionBaseDocument(User user, ReactionType type, String comment){
        this.user = user;
        this.type = type;
        setComment(comment);
    }

    public void setComment(String comment){
        if(this.type == ReactionType.COMMENT){
          this.comment = comment;
        } else {
            this.comment = null;
        }
    }

    public enum ReactionType{
        LIKE, DISLIKE, COMMENT
    }
}