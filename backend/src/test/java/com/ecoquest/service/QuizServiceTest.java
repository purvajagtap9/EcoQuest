package com.ecoquest.service;

import com.ecoquest.dto.learning.QuizAttemptRequest;
import com.ecoquest.dto.learning.QuizAttemptResultResponse;
import com.ecoquest.entity.Quiz;
import com.ecoquest.entity.QuizAttempt;
import com.ecoquest.entity.embedded.Question;
import com.ecoquest.entity.enums.CoinTransactionType;
import com.ecoquest.repository.QuizAttemptRepository;
import com.ecoquest.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock private QuizRepository quizRepository;
    @Mock private QuizAttemptRepository quizAttemptRepository;
    @Mock private EcoCoinService ecoCoinService;
    @Mock private GamificationService gamificationService;
    @Mock private LeaderboardService leaderboardService;
    @Mock private RealtimeLeaderboardService realtimeLeaderboardService;

    @InjectMocks
    private QuizService quizService;

    private Quiz quiz;

    @BeforeEach
    void setUp() {
        quiz = Quiz.builder()
                .id("quiz-1")
                .title("Waste Management Basics")
                .questions(List.of(
                        new Question("q1", "Which is wet waste?", List.of("Banana peel", "Plastic", "Metal", "Glass"), 0, "Banana peel is biodegradable"),
                        new Question("q2", "Which bin is for dry waste?", List.of("Green", "Blue", "Red", "Black"), 1, "Blue is typical for dry waste")
                ))
                .coinsPerCorrectAnswer(20)
                .xpPerCorrectAnswer(40)
                .published(true)
                .build();
    }

    @Test
    void submitAttempt_firstAttemptAllCorrect_awardsFullReward() {
        when(quizRepository.findByIdAndPublishedTrue("quiz-1")).thenReturn(Optional.of(quiz));
        when(quizAttemptRepository.existsByStudentIdAndQuizId("student-1", "quiz-1")).thenReturn(false);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> {
            QuizAttempt a = inv.getArgument(0);
            a.setId("attempt-1");
            return a;
        });

        QuizAttemptRequest request = new QuizAttemptRequest();
        request.setAnswers(Map.of("q1", 0, "q2", 1));

        QuizAttemptResultResponse result = quizService.submitAttempt("student-1", "quiz-1", request);

        assertThat(result.getScore()).isEqualTo(2);
        assertThat(result.getTotalQuestions()).isEqualTo(2);
        assertThat(result.isRewarded()).isTrue();
        assertThat(result.getCoinsEarned()).isEqualTo(40); // 2 correct * 20
        assertThat(result.getXpEarned()).isEqualTo(80);    // 2 correct * 40
        assertThat(result.getResults()).allMatch(r -> r.isCorrect());

        verify(ecoCoinService).award("student-1", 40, 80, CoinTransactionType.QUIZ, "Quiz: Waste Management Basics", "attempt-1");
        verify(gamificationService).recordActivity("student-1");
        verify(gamificationService).checkAndAwardBadges("student-1");
    }

    @Test
    void submitAttempt_partiallyCorrect_scoresAccuratelyAndShowsExplanations() {
        when(quizRepository.findByIdAndPublishedTrue("quiz-1")).thenReturn(Optional.of(quiz));
        when(quizAttemptRepository.existsByStudentIdAndQuizId("student-1", "quiz-1")).thenReturn(false);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        QuizAttemptRequest request = new QuizAttemptRequest();
        request.setAnswers(Map.of("q1", 0, "q2", 2)); // q2 wrong (correct is 1)

        QuizAttemptResultResponse result = quizService.submitAttempt("student-1", "quiz-1", request);

        assertThat(result.getScore()).isEqualTo(1);
        assertThat(result.getCoinsEarned()).isEqualTo(20);
        var q2Result = result.getResults().stream().filter(r -> r.getQuestionId().equals("q2")).findFirst().orElseThrow();
        assertThat(q2Result.isCorrect()).isFalse();
        assertThat(q2Result.getExplanation()).isEqualTo("Blue is typical for dry waste");
    }

    @Test
    void submitAttempt_secondAttempt_doesNotAwardRewardAgain() {
        when(quizRepository.findByIdAndPublishedTrue("quiz-1")).thenReturn(Optional.of(quiz));
        when(quizAttemptRepository.existsByStudentIdAndQuizId("student-1", "quiz-1")).thenReturn(true); // already attempted
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        QuizAttemptRequest request = new QuizAttemptRequest();
        request.setAnswers(Map.of("q1", 0, "q2", 1));

        QuizAttemptResultResponse result = quizService.submitAttempt("student-1", "quiz-1", request);

        assertThat(result.getScore()).isEqualTo(2); // still graded correctly
        assertThat(result.isRewarded()).isFalse();
        assertThat(result.getCoinsEarned()).isZero();
        assertThat(result.getXpEarned()).isZero();

        verify(ecoCoinService, never()).award(any(), anyInt(), anyInt(), any(), any(), any());
        verify(gamificationService, never()).recordActivity(any());
    }

    @Test
    void submitAttempt_unansweredQuestion_isMarkedIncorrect() {
        when(quizRepository.findByIdAndPublishedTrue("quiz-1")).thenReturn(Optional.of(quiz));
        when(quizAttemptRepository.existsByStudentIdAndQuizId("student-1", "quiz-1")).thenReturn(false);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        QuizAttemptRequest request = new QuizAttemptRequest();
        request.setAnswers(Map.of("q1", 0)); // q2 left unanswered

        QuizAttemptResultResponse result = quizService.submitAttempt("student-1", "quiz-1", request);

        assertThat(result.getScore()).isEqualTo(1);
        var q2Result = result.getResults().stream().filter(r -> r.getQuestionId().equals("q2")).findFirst().orElseThrow();
        assertThat(q2Result.getYourAnswerIndex()).isNull();
        assertThat(q2Result.isCorrect()).isFalse();
    }
}
