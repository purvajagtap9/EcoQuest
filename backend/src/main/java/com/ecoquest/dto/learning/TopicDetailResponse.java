package com.ecoquest.dto.learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicDetailResponse {
    private String id;
    private String title;
    private String description;
    private List<String> learningObjectives;
    private String coverImageUrl;
    private List<LessonResponse> lessons;
}
