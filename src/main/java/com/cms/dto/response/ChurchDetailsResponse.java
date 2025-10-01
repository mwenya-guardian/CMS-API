package com.cms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChurchDetailsResponse {
    
    private String id;
    private String name;
    private String address;
    private String poBox;
    private String city;
    private String province;
    private String country;
    private String tel;
    private List<String> cell;
    private String email;
    private String website;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
