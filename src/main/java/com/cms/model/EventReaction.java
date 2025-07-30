package com.cms.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Document(collection = "event_reactions")
public class EventReaction extends BaseDocument {

    @DBRef(lazy = true)
    @Indexed
    private Event event;

    @Id
    private String id;

    @NotBlank
    @Size(max = 50)
    private String type; // e.g., LIKE, LOVE, etc.

    @DBRef(lazy = true)
    @Indexed
    private User user;
}
