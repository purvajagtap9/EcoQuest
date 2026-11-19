package com.ecoquest.util;

/**
 * XP -> level/title mapping (spec section 17).
 * Level 1: 0-499, Level 2: 500-999, Level 3: 1000-1999, Level 4: 2000-3499, Level 5: 3500+
 */
public class LevelUtil {

    private LevelUtil() {
    }

    public record LevelInfo(int level, String title, int xpIntoLevel, int xpForNextLevel) {
    }

    private static final int[] THRESHOLDS = {0, 500, 1000, 2000, 3500};
    private static final String[] TITLES = {
            "🌱 Eco Beginner",
            "🌿 Eco Explorer",
            "🌳 Green Guardian",
            "🌍 Eco Champion",
            "🌍 Eco Champion" // level 5+ keeps the top title
    };

    public static LevelInfo forXp(int xp) {
        int levelIndex = 0;
        for (int i = THRESHOLDS.length - 1; i >= 0; i--) {
            if (xp >= THRESHOLDS[i]) {
                levelIndex = i;
                break;
            }
        }
        int level = levelIndex + 1;
        String title = TITLES[levelIndex];
        int nextThreshold = levelIndex + 1 < THRESHOLDS.length ? THRESHOLDS[levelIndex + 1] : -1;
        int xpForNextLevel = nextThreshold == -1 ? 0 : nextThreshold;
        return new LevelInfo(level, title, xp, xpForNextLevel);
    }
}
