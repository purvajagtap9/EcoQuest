package com.ecoquest.controller;

import com.ecoquest.dto.teacher.RejectSubmissionRequest;
import com.ecoquest.dto.teacher.SubmissionDetailResponse;
import com.ecoquest.dto.teacher.SubmissionListItemResponse;
import com.ecoquest.entity.enums.SubmissionStatus;
import com.ecoquest.security.CustomUserDetails;
import com.ecoquest.service.SubmissionReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/submissions")
@RequiredArgsConstructor
@Tag(name = "Teacher - Submissions", description = "Review queue and approve/reject, scoped to the teacher's school")
public class TeacherSubmissionController {

    private final SubmissionReviewService reviewService;

    @GetMapping
    public Page<SubmissionListItemResponse> listSubmissions(
            @RequestParam(required = false) SubmissionStatus status,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return reviewService.listForTeacher(principal.getUser().getId(), status, pageable);
    }

    @GetMapping("/{id}")
    public SubmissionDetailResponse getSubmission(@PathVariable String id,
                                                   @AuthenticationPrincipal CustomUserDetails principal) {
        return reviewService.getDetailForTeacher(principal.getUser().getId(), id);
    }

    @PutMapping("/{id}/approve")
    public SubmissionDetailResponse approve(@PathVariable String id,
                                             @AuthenticationPrincipal CustomUserDetails principal) {
        return reviewService.approveAsTeacher(principal.getUser().getId(), id);
    }

    @PutMapping("/{id}/reject")
    public SubmissionDetailResponse reject(@PathVariable String id,
                                            @Valid @RequestBody RejectSubmissionRequest request,
                                            @AuthenticationPrincipal CustomUserDetails principal) {
        return reviewService.rejectAsTeacher(principal.getUser().getId(), id, request.getRejectionReason());
    }
}
