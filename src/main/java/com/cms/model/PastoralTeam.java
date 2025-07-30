package com.cms.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pastoral_team")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PastoralTeam extends BaseDocument {

    @Id
    private String id;

    @NotBlank
    @Size(max = 100)
    private String position;

    @NotBlank
    @Size(max = 200)
    private String fullName;

    @Size(max = 100)
    private String positionType;

    @Size(max = 1000)
    private String biography;

    @Size(max = 500)
    private String photoUrl;

    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    private Integer displayOrder;

    private Boolean active = true;
}
