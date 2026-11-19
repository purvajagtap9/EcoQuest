package com.ecoquest.controller;

import com.ecoquest.dto.learning.TopicDetailResponse;
import com.ecoquest.dto.learning.TopicResponse;
import com.ecoquest.service.EvsLearningService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public browse endpoints - no auth required (see SecurityConfig: /api/topics/** is permitAll).
 * `lang` defaults to English; pass hi or mr to get that language where translated content
 * exists (falls back to English per record if a translation is missing - see LocalizedText.get).
 */
@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@Tag(name = "Learning", description = "Public EVS topic and lesson browsing")
public class LearningController {

    private final EvsLearningService learningService;

    @GetMapping
    public List<TopicResponse> listTopics(@RequestParam(defaultValue = "en") String lang) {
        return learningService.listPublishedTopics(lang);
    }

    @GetMapping("/{id}")
    public TopicDetailResponse getTopic(@PathVariable String id, @RequestParam(defaultValue = "en") String lang) {
        return learningService.getTopicDetail(id, lang);
    }
}
