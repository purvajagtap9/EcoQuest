package com.ecoquest.controller;

import com.ecoquest.dto.admin.QuizAdminRequest;
import com.ecoquest.dto.admin.QuizAdminResponse;
import com.ecoquest.service.QuizService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/quizzes")
@RequiredArgsConstructor
@Tag(name = "Admin - Quizzes", description = "Manage quizzes")
public class AdminQuizController {

    private final QuizService quizService;

    @GetMapping
    public List<QuizAdminResponse> listQuizzes(@RequestParam(required = false) String topicId) {
        return quizService.listQuizzesAdmin(topicId);
    }

    @PostMapping
    public ResponseEntity<QuizAdminResponse> createQuiz(@Valid @RequestBody QuizAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.createQuiz(request));
    }

    @PutMapping("/{id}")
    public QuizAdminResponse updateQuiz(@PathVariable String id, @Valid @RequestBody QuizAdminRequest request) {
        return quizService.updateQuiz(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable String id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }
}
