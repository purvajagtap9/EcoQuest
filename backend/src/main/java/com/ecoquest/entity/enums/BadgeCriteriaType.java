package com.ecoquest.entity.enums;

/**
 * What a badge's unlock condition is measured against.
 * Paired with Badge.criteriaThreshold and, where relevant, Badge.criteriaCategory.
 */
public enum BadgeCriteriaType {
    QUESTS_COMPLETED_TOTAL,
    QUESTS_COMPLETED_IN_CATEGORY,
    STREAK_DAYS,
    ECO_COINS_EARNED_TOTAL,
    QUIZZES_COMPLETED_TOTAL
}
