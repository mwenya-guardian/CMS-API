package com.cms.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document
public class QuoteReaction extends ReactionBaseDocument {
    @DBRef(lazy = true)
    @Indexed
    private Quote quote;

    public QuoteReaction(Quote quote, User user, ReactionType type, String comment){
        super(user, type, comment);
        this.quote = quote;
    }
}
