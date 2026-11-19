package com.ecoquest.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdjustCoinsRequest {

    @NotNull(message = "amount is required")
    private Integer amount; // positive or negative, not zero

    @NotBlank(message = "description is required")
    private String description;
}
