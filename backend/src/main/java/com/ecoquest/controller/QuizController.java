package com.ecoquest.controller;

import com.ecoquest.dto.learning.QuizAttemptRequest;
import com.ecoquest.dto.learning.QuizAttemptResultResponse;
import com.ecoquest.dto.learning.QuizResponse;
import com.ecoquest.dto.learning.QuizSummaryResponse;
import com.ecoquest.security.CustomUserDetails;
import com.ecoquest.service.QuizService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quizzes", description = "Public quiz fetch and student attempt submission")
public class QuizController {

    private final QuizService quizService;

    /** Public - lets the frontend show "Take the quiz" links from a topic page without an admin token. */
    @GetMapping
    public List<QuizSummaryResponse> listByTopic(@RequestParam String topicId) {
        return quizService.listPublishedQuizSummaries(topicId);
    }

    /** Public (see SecurityConfig) - answers are never included here. */
    @GetMapping("/{id}")
    public QuizResponse getQuiz(@PathVariable String id) {
        return quizService.getQuiz(id);
    }

    /** Student-only (see SecurityConfig) - grades the attempt and (first attempt only) rewards. */
    @PostMapping("/{id}/attempt")
    public QuizAttemptResultResponse submitAttempt(@PathVariable String id,
                                                     @Valid @RequestBody QuizAttemptRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails principal) {
        return quizService.submitAttempt(principal.getUser().getId(), id, request);
    }
}
