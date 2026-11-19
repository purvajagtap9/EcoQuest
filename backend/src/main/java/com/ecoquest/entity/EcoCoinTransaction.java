package com.ecoquest.entity;

import com.ecoquest.entity.enums.CoinTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Append-only ledger — this, not User.studentProfile.ecoCoins, is the actual source of truth
 * (spec #16: "Do NOT simply store one editable balance"). User.studentProfile.ecoCoins is a
 * denormalized cache kept in sync by EcoCoinService every time a transaction is inserted here.
 * `referenceId` + `type` together let us prevent duplicate rewards for the same
 * quest/quiz/badge event (business rules #1-3).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "eco_coin_transactions")
public class EcoCoinTransaction {

    @Id
    private String id;

    @Indexed
    private String studentId;

    private Integer amount; // positive or negative (ADMIN_ADJUSTMENT can be negative)
    private CoinTransactionType type;
    private String description;

    @Indexed
    private String referenceId; // e.g. the QuestSubmission id, QuizAttempt id, or Badge id

    @CreatedDate
    private Instant createdAt;
}
