package com.ecoquest.entity.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Embedded inside User.studentProfile. A student has exactly one streak counter, so this
 * doesn't warrant its own collection — it's updated in place whenever a qualifying activity
 * (approved quest, completed quiz) happens.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Streak {
    private Integer currentStreak = 0;
    private Integer longestStreak = 0;
    private LocalDate lastActivityDate;
}
