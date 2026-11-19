package com.ecoquest.controller;

import com.ecoquest.dto.admin.ImpactFormulaRequest;
import com.ecoquest.dto.admin.ImpactFormulaResponse;
import com.ecoquest.dto.impact.ImpactSummaryResponse;
import com.ecoquest.entity.enums.QuestCategory;
import com.ecoquest.service.ImpactService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Impact", description = "Platform-wide impact summary and configurable impact formulas")
public class AdminImpactController {

    private final ImpactService impactService;

    @GetMapping("/analytics/impact")
    public ImpactSummaryResponse getGlobalImpact() {
        return impactService.getGlobalSummary();
    }

    @GetMapping("/impact-formulas")
    public List<ImpactFormulaResponse> listFormulas() {
        return impactService.listFormulas();
    }

    /** Creates or replaces the override for request.getCategory(). */
    @PutMapping("/impact-formulas")
    public ImpactFormulaResponse upsertFormula(@Valid @RequestBody ImpactFormulaRequest request) {
        return impactService.upsertFormula(request);
    }

    @DeleteMapping("/impact-formulas/{category}")
    public ResponseEntity<Void> resetFormula(@PathVariable QuestCategory category) {
        impactService.resetFormulaToDefault(category);
        return ResponseEntity.noContent().build();
    }
}
