package com.ecoquest.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolResponse {
    private String id;
    private String name;
    private String city;
    private String state;
    private Double campusLatitude;
    private Double campusLongitude;
    private Instant createdAt;
}
