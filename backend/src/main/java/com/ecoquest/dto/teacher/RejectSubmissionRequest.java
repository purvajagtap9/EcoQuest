package com.ecoquest.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectSubmissionRequest {

    @NotBlank(message = "rejectionReason is required")
    private String rejectionReason;
}
