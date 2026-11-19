package com.ecoquest.dto.admin;

import com.ecoquest.dto.common.LocalizedTextDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicAdminResponse {
    private String id;
    private LocalizedTextDto title;
    private LocalizedTextDto description;
    private List<LocalizedTextDto> learningObjectives;
    private String coverImageUrl;
    private Integer orderIndex;
    private boolean published;
    private Instant createdAt;
}
