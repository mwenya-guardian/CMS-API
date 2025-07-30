package com.cms.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Document(collection = "church_details")
public class ChurchDetails extends BaseDocument {

    @Id
    private String id;

    @NotBlank
    @Size(max = 200)
    @Field("name")
    private String name;

    @NotBlank
    @Size(max = 500)
    @Field("address")
    private String address;

    @Size(max = 100)
    @Field("document_name")
    private String documentName;

    @Size(max = 200)
    @Field("greeting")
    private String greeting;

    @Size(max = 500)
    @Field("message")
    private String message;

    @Size(max = 100)
    @Field("po_box")
    private String poBox;

    @Size(max = 100)
    @Field("city")
    private String city;

    @Size(max = 100)
    @Field("province")
    private String province;

    @Size(max = 100)
    @Field("country")
    private String country;

    @Size(max = 20)
    @Field("tel")
    private String tel;

    @Size(max = 20)
    @Field("cell")
    private String cell;

    @Size(max = 100)
    @Field("email")
    private String email;

    @Size(max = 200)
    @Field("website")
    private String website;
}
