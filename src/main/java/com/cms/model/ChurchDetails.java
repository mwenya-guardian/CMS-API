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
import java.util.List;

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
    private List<String> cell;

    @Size(max = 100)
    @Field("email")
    private String email;

    @Size(max = 200)
    @Field("website")
    private String website;
}
