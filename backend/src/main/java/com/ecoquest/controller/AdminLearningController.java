package com.ecoquest.controller;

import com.ecoquest.dto.admin.LessonAdminRequest;
import com.ecoquest.dto.admin.LessonAdminResponse;
import com.ecoquest.dto.admin.TopicAdminRequest;
import com.ecoquest.dto.admin.TopicAdminResponse;
import com.ecoquest.service.EvsLearningService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Learning", description = "Manage EVS topics and lessons")
public class AdminLearningController {

    private final EvsLearningService learningService;

    // ---------- Topics ----------

    @GetMapping("/topics")
    public List<TopicAdminResponse> listTopics() {
        return learningService.listAllTopicsAdmin();
    }

    @PostMapping("/topics")
    public ResponseEntity<TopicAdminResponse> createTopic(@Valid @RequestBody TopicAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(learningService.createTopic(request));
    }

    @PutMapping("/topics/{id}")
    public TopicAdminResponse updateTopic(@PathVariable String id, @Valid @RequestBody TopicAdminRequest request) {
        return learningService.updateTopic(id, request);
    }

    @DeleteMapping("/topics/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable String id) {
        learningService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Lessons ----------

    @GetMapping("/lessons")
    public List<LessonAdminResponse> listLessons(@RequestParam String topicId) {
        return learningService.listLessonsAdmin(topicId);
    }

    @PostMapping("/lessons")
    public ResponseEntity<LessonAdminResponse> createLesson(@Valid @RequestBody LessonAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(learningService.createLesson(request));
    }

    @PutMapping("/lessons/{id}")
    public LessonAdminResponse updateLesson(@PathVariable String id, @Valid @RequestBody LessonAdminRequest request) {
        return learningService.updateLesson(id, request);
    }

    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable String id) {
        learningService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
}
