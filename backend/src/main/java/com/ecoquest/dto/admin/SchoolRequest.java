package com.ecoquest.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SchoolRequest {

    @NotBlank(message = "School name is required")
    private String name;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    private Double campusLatitude;
    private Double campusLongitude;
}
