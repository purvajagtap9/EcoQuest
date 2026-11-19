package com.ecoquest.controller;

import com.ecoquest.dto.quest.QuestSubmissionResponse;
import com.ecoquest.security.CustomUserDetails;
import com.ecoquest.service.QuestSubmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Matches the spec's exact route: POST /api/quests/{questId}/submit (not nested under
 * /api/student/** — see SecurityConfig, which grants this one specifically to STUDENT role).
 */
@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
@Tag(name = "Quest Submission", description = "Student evidence upload for a quest")
public class QuestSubmissionController {

    private final QuestSubmissionService submissionService;

    @PostMapping(value = "/{questId}/submit", consumes = "multipart/form-data")
    public ResponseEntity<QuestSubmissionResponse> submit(
            @PathVariable String questId,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @AuthenticationPrincipal CustomUserDetails principal) {

        QuestSubmissionResponse response = submissionService.submitProof(
                principal.getUser().getId(), questId, image, description, latitude, longitude);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
