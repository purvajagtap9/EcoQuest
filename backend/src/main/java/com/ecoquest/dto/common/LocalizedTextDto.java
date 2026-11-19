package com.ecoquest.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalizedTextDto {

    @NotBlank(message = "English text is required")
    private String en;

    private String hi;
    private String mr;
}
