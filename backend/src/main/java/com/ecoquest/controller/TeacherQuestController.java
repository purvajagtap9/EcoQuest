package com.ecoquest.controller;

import com.ecoquest.dto.quest.QuestRequest;
import com.ecoquest.dto.quest.QuestResponse;
import com.ecoquest.security.CustomUserDetails;
import com.ecoquest.service.QuestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * A teacher can create quests (auto-scoped to their own assignedSchoolId - see
 * QuestService.createQuestAsTeacher) and can only update/delete quests THEY created
 * (QuestService.assertTeacherOwnsQuest) - they can't touch admin-created or another
 * teacher's quests through this controller.
 */
@RestController
@RequestMapping("/api/teacher/quests")
@RequiredArgsConstructor
@Tag(name = "Teacher - Quests", description = "Create and manage quests for your own school")
public class TeacherQuestController {

    private final QuestService questService;

    @GetMapping
    public Page<QuestResponse> listMyQuests(Pageable pageable, @AuthenticationPrincipal CustomUserDetails principal) {
        return questService.listQuestsForTeacher(principal.getUser().getId(), pageable);
    }

    @PostMapping
    public ResponseEntity<QuestResponse> createQuest(@Valid @RequestBody QuestRequest request,
                                                       @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questService.createQuestAsTeacher(request, principal.getUser().getId()));
    }

    @PutMapping("/{id}")
    public QuestResponse updateQuest(@PathVariable String id, @Valid @RequestBody QuestRequest request,
                                      @AuthenticationPrincipal CustomUserDetails principal) {
        return questService.updateQuestAsTeacher(id, request, principal.getUser().getId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuest(@PathVariable String id, @AuthenticationPrincipal CustomUserDetails principal) {
        questService.deleteQuestAsTeacher(id, principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
