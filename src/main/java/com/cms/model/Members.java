package com.cms.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Members extends BaseDocument {

    @Id
    private String id;

    @NotBlank
    @Size(max = 100)
    private String position;

    @NotBlank
    @Size(max = 200)
    private String firstname;

    @NotBlank
    @Size(max = 200)
    private String lastname;

    @Size(max = 100)
    private String positionType;

    @Size(max = 500)
    private String photoUrl;

    @Size(max = 100)
    @Indexed(unique = true)
    private String email;

    @Size(max = 20)
    @NotBlank
    @Indexed(unique = true)
    private String phone;
}
