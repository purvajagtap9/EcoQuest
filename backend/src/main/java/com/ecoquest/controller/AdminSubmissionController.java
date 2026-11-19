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

/** Admin can review/override ANY submission regardless of school - business rule #5. */
@RestController
@RequestMapping("/api/admin/submissions")
@RequiredArgsConstructor
@Tag(name = "Admin - Submissions", description = "Unrestricted submission review and override")
public class AdminSubmissionController {

    private final SubmissionReviewService reviewService;

    @GetMapping
    public Page<SubmissionListItemResponse> listSubmissions(
            @RequestParam(required = false) SubmissionStatus status, Pageable pageable) {
        return reviewService.listForAdmin(status, pageable);
    }

    @GetMapping("/{id}")
    public SubmissionDetailResponse getSubmission(@PathVariable String id) {
        return reviewService.getDetailForAdmin(id);
    }

    @PutMapping("/{id}/approve")
    public SubmissionDetailResponse approve(@PathVariable String id, @AuthenticationPrincipal CustomUserDetails principal) {
        return reviewService.approveAsAdmin(principal.getUser().getId(), id);
    }

    @PutMapping("/{id}/reject")
    public SubmissionDetailResponse reject(@PathVariable String id,
                                            @Valid @RequestBody RejectSubmissionRequest request,
                                            @AuthenticationPrincipal CustomUserDetails principal) {
        return reviewService.rejectAsAdmin(principal.getUser().getId(), id, request.getRejectionReason());
    }
}
