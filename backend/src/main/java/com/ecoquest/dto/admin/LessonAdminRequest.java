package com.ecoquest.dto.admin;

import com.ecoquest.dto.common.LocalizedTextDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonAdminRequest {

    @NotBlank(message = "topicId is required")
    private String topicId;

    @Valid
    @NotNull(message = "title is required")
    private LocalizedTextDto title;

    @Valid
    @NotNull(message = "body is required")
    private LocalizedTextDto body;

    @Valid
    private LocalizedTextDto localContextNote;

    private String imageUrl;
    private Integer orderIndex;
}
