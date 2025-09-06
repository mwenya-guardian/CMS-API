package com.cms.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
@Getter
@Setter
@NoArgsConstructor
public class PublicationReaction extends ReactionBaseDocument {
    @DBRef(lazy = true)
    @Indexed
    private Publication publication;

    public PublicationReaction(Publication publication, User user, ReactionType type, String comment) {
        super(user, type, comment);
        this.publication = publication;
    }
}
