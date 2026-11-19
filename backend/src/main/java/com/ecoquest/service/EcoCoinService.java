package com.ecoquest.service;

import com.ecoquest.entity.EcoCoinTransaction;
import com.ecoquest.entity.User;
import com.ecoquest.entity.enums.CoinTransactionType;
import com.ecoquest.entity.enums.Role;
import com.ecoquest.exception.ApiException;
import com.ecoquest.repository.EcoCoinTransactionRepository;
import com.ecoquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * The ONLY place that writes to User.studentProfile.ecoCoins/xp — students never modify their
 * own balance directly (business rule #7), and every coin change is ledgered in
 * EcoCoinTransaction (business rule #8 / spec #16), never just a bare balance edit.
 * Bootstrapped in Phase 8 for the award path; this phase (12) adds history lookup and admin
 * manual adjustments, now that quizzes (Phase 8) and quests (Phase 11) both feed the ledger.
 */
@Service
@RequiredArgsConstructor
public class EcoCoinService {

    private final EcoCoinTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    /**
     * Atomically increments a student's cached ecoCoins and xp, and (if coinAmount != 0)
     * appends a ledger entry. xpAmount is not ledgered (spec doesn't call for an XP ledger,
     * only coins), but is updated in the same atomic $inc as the coins to avoid a second
     * round-trip.
     */
    public void award(String studentId, int coinAmount, int xpAmount, CoinTransactionType type,
                       String description, String referenceId) {

        Update update = new Update();
        if (coinAmount != 0) {
            update.inc("studentProfile.ecoCoins", coinAmount);
        }
        if (xpAmount != 0) {
            update.inc("studentProfile.xp", xpAmount);
        }
        if (coinAmount != 0 || xpAmount != 0) {
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("_id").is(studentId)),
                    update,
                    User.class);
        }

        if (coinAmount != 0) {
            transactionRepository.save(EcoCoinTransaction.builder()
                    .studentId(studentId)
                    .amount(coinAmount)
                    .type(type)
                    .description(description)
                    .referenceId(referenceId)
                    .createdAt(Instant.now())
                    .build());
        }
    }

    public Page<EcoCoinTransaction> getHistory(String studentId, Pageable pageable) {
        return transactionRepository.findByStudentId(studentId, pageable);
    }

    /**
     * Admin manual correction (spec's ADMIN_ADJUSTMENT type). Can be positive or negative, but
     * is not allowed to push a student's balance below zero - gamification balances shouldn't
     * go negative even for corrections.
     */
    public void adjustBalance(String studentId, int amount, String description, String adminUserId) {
        if (amount == 0) {
            throw ApiException.badRequest("Adjustment amount cannot be zero");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> ApiException.notFound("Student not found"));
        if (student.getRole() != Role.STUDENT || student.getStudentProfile() == null) {
            throw ApiException.badRequest("User is not a student");
        }

        int currentBalance = student.getStudentProfile().getEcoCoins() == null ? 0 : student.getStudentProfile().getEcoCoins();
        if (currentBalance + amount < 0) {
            throw ApiException.badRequest("Adjustment would result in a negative balance (current: " + currentBalance + ")");
        }

        award(studentId, amount, 0, CoinTransactionType.ADMIN_ADJUSTMENT,
                description, "admin:" + adminUserId);
    }
}
