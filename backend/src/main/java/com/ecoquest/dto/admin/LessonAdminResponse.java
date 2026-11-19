package com.ecoquest.dto.admin;

import com.ecoquest.dto.common.LocalizedTextDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonAdminResponse {
    private String id;
    private String topicId;
    private LocalizedTextDto title;
    private LocalizedTextDto body;
    private LocalizedTextDto localContextNote;
    private String imageUrl;
    private Integer orderIndex;
}
