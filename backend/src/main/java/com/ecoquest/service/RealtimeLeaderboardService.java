package com.ecoquest.service;

import com.ecoquest.dto.leaderboard.RankChangeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Called after any reward-granting event with the student's rank captured BEFORE the reward
 * (see call sites in SubmissionReviewService.approve and QuizService.submitAttempt). Compares
 * against the rank now and, if it changed, pushes to /user/{userId}/queue/leaderboard - a
 * connected client subscribed to /user/queue/leaderboard receives it live (spec #21's
 * "🏆 You moved from #15 → #11!" example). If the student isn't connected, this is a no-op;
 * there's no persistence/replay of missed notifications (out of scope for the prototype).
 */
@Service
@RequiredArgsConstructor
public class RealtimeLeaderboardService {

    private final SimpMessagingTemplate messagingTemplate;
    private final LeaderboardService leaderboardService;

    public void notifyIfRankChanged(String studentId, int oldRank) {
        int newRank = leaderboardService.getStudentRank(studentId);
        if (newRank <= 0 || newRank == oldRank) {
            return;
        }

        String arrow = newRank < oldRank ? "🏆 You moved up" : "You moved";
        String message = String.format("%s from #%d → #%d!", arrow, oldRank, newRank);

        messagingTemplate.convertAndSendToUser(studentId, "/queue/leaderboard",
                RankChangeMessage.builder()
                        .oldRank(oldRank)
                        .newRank(newRank)
                        .message(message)
                        .build());
    }
}
