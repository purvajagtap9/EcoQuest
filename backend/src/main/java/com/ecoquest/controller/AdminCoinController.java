package com.ecoquest.controller;

import com.ecoquest.dto.admin.AdjustCoinsRequest;
import com.ecoquest.dto.student.CoinTransactionResponse;
import com.ecoquest.entity.EcoCoinTransaction;
import com.ecoquest.security.CustomUserDetails;
import com.ecoquest.service.EcoCoinService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/students/{id}")
@RequiredArgsConstructor
@Tag(name = "Admin - Eco-Coins", description = "Manual balance adjustments and transaction history")
public class AdminCoinController {

    private final EcoCoinService ecoCoinService;

    @GetMapping("/coin-transactions")
    public Page<CoinTransactionResponse> listTransactions(@PathVariable String id, Pageable pageable) {
        return ecoCoinService.getHistory(id, pageable).map(this::toResponse);
    }

    @PostMapping("/coins/adjust")
    public ResponseEntity<Void> adjustCoins(@PathVariable String id,
                                             @Valid @RequestBody AdjustCoinsRequest request,
                                             @AuthenticationPrincipal CustomUserDetails principal) {
        ecoCoinService.adjustBalance(id, request.getAmount(), request.getDescription(), principal.getUser().getId());
        return ResponseEntity.ok().build();
    }

    private CoinTransactionResponse toResponse(EcoCoinTransaction t) {
        return CoinTransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType())
                .description(t.getDescription())
                .referenceId(t.getReferenceId())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
