package com.cms.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Document(collection = "publication_reactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PublicationReaction extends BaseDocument {

    @Id
    private String id;

    @NotBlank
    @Size(max = 50)
    private String type; // e.g., LIKE, LOVE, etc.

    @DBRef(lazy = true)
    @Indexed
    private Publication publication;

    @DBRef(lazy = true)
    @Indexed
    private User user;

}
