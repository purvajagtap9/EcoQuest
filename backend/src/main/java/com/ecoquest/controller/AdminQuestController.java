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

@RestController
@RequestMapping("/api/admin/quests")
@RequiredArgsConstructor
@Tag(name = "Admin - Quests", description = "Unrestricted quest management")
public class AdminQuestController {

    private final QuestService questService;

    @GetMapping
    public Page<QuestResponse> listQuests(Pageable pageable) {
        return questService.listAllQuestsAdmin(pageable);
    }

    @PostMapping
    public ResponseEntity<QuestResponse> createQuest(@Valid @RequestBody QuestRequest request,
                                                       @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questService.createQuestAsAdmin(request, principal.getUser().getId()));
    }

    @PutMapping("/{id}")
    public QuestResponse updateQuest(@PathVariable String id, @Valid @RequestBody QuestRequest request) {
        return questService.updateQuestAsAdmin(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuest(@PathVariable String id) {
        questService.deleteQuestAsAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
