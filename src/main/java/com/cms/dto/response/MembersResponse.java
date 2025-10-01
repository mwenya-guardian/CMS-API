package com.cms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembersResponse {
    
    private String id;
    private String position;
    private String firstname;
    private String lastname;
    private String positionType;
    private String photoUrl;
    private String email;
    private String phone;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
