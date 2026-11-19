package com.ecoquest.dto.student;

import com.ecoquest.entity.enums.CoinTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoinTransactionResponse {
    private String id;
    private Integer amount;
    private CoinTransactionType type;
    private String description;
    private String referenceId;
    private Instant createdAt;
}
