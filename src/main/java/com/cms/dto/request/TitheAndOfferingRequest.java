package com.cms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TitheAndOfferingRequest {
    
    @NotBlank(message = "Title is required")
    private String title;

    @NotEmpty(message = "At least one payment method is required")
    private List<String> method;
}
