package com.ecoquest.controller;

import com.ecoquest.dto.admin.BadgeAdminRequest;
import com.ecoquest.dto.admin.BadgeAdminResponse;
import com.ecoquest.service.BadgeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/badges")
@RequiredArgsConstructor
@Tag(name = "Admin - Badges", description = "Manage badges")
public class AdminBadgeController {

    private final BadgeService badgeService;

    @GetMapping
    public List<BadgeAdminResponse> listBadges() {
        return badgeService.listAllAdmin();
    }

    @PostMapping
    public ResponseEntity<BadgeAdminResponse> createBadge(@Valid @RequestBody BadgeAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(badgeService.createBadge(request));
    }

    @PutMapping("/{id}")
    public BadgeAdminResponse updateBadge(@PathVariable String id, @Valid @RequestBody BadgeAdminRequest request) {
        return badgeService.updateBadge(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBadge(@PathVariable String id) {
        badgeService.deleteBadge(id);
        return ResponseEntity.noContent().build();
    }
}
