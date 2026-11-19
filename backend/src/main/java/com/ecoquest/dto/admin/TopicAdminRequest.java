package com.ecoquest.dto.admin;

import com.ecoquest.dto.common.LocalizedTextDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class TopicAdminRequest {

    @Valid
    @NotNull(message = "title is required")
    private LocalizedTextDto title;

    @Valid
    @NotNull(message = "description is required")
    private LocalizedTextDto description;

    @Valid
    private List<LocalizedTextDto> learningObjectives;

    private String coverImageUrl;
    private Integer orderIndex;
    private boolean published = true;
}
