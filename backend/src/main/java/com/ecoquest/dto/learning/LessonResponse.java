package com.ecoquest.dto.learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonResponse {
    private String id;
    private String topicId;
    private String title;
    private String body;
    private String localContextNote;
    private String imageUrl;
    private Integer orderIndex;
}
