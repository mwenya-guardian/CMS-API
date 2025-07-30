package com.cms.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Setter
@Getter
public class Welcome {
    @NotBlank
    private String welcomeMessage;
    @NotBlank
    private String welcomeMissionMessage;

}
