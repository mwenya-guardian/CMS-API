package com.cms.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
@Document(collection = "giving")
public class TitheAndOffering extends BaseDocument{
    @Id
    private String id;
    @NotBlank
    private String title;
    @NotEmpty
    private List<String> method;

}
