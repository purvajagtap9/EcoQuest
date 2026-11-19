package com.ecoquest.service;

import com.ecoquest.dto.admin.QuestionAdminDto;
import com.ecoquest.dto.admin.QuizAdminRequest;
import com.ecoquest.dto.admin.QuizAdminResponse;
import com.ecoquest.dto.learning.*;
import com.ecoquest.entity.Quiz;
import com.ecoquest.entity.QuizAttempt;
import com.ecoquest.entity.embedded.Question;
import com.ecoquest.entity.enums.CoinTransactionType;
import com.ecoquest.exception.ApiException;
import com.ecoquest.repository.QuizAttemptRepository;
import com.ecoquest.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final EcoCoinService ecoCoinService;
    private final GamificationService gamificationService;
    private final LeaderboardService leaderboardService;
    private final RealtimeLeaderboardService realtimeLeaderboardService;

    // ---------- Public ----------

    public List<QuizSummaryResponse> listPublishedQuizSummaries(String topicId) {
        return quizRepository.findByTopicIdAndPublishedTrue(topicId).stream()
                .map(q -> QuizSummaryResponse.builder()
                        .id(q.getId())
                        .title(q.getTitle())
                        .questionCount(q.getQuestions().size())
                        .build())
                .toList();
    }

    public QuizResponse getQuiz(String quizId) {
        Quiz quiz = quizRepository.findByIdAndPublishedTrue(quizId)
                .orElseThrow(() -> ApiException.notFound("Quiz not found"));

        List<QuestionPublicDto> questions = quiz.getQuestions().stream()
                .map(q -> QuestionPublicDto.builder()
                        .id(q.getId())
                        .text(q.getText())
                        .options(q.getOptions())
                        .build())
                .toList();

        return QuizResponse.builder()
                .id(quiz.getId())
                .topicId(quiz.getTopicId())
                .title(quiz.getTitle())
                .questions(questions)
                .build();
    }

    /**
     * Grades the attempt, records it, and — ONLY on the student's first attempt at this quiz
     * (spec #9: "Prevent duplicate/repeated reward abuse") — awards coins/XP via EcoCoinService.
     */
    public QuizAttemptResultResponse submitAttempt(String studentId, String quizId, QuizAttemptRequest request) {
        Quiz quiz = quizRepository.findByIdAndPublishedTrue(quizId)
                .orElseThrow(() -> ApiException.notFound("Quiz not found"));

        boolean isFirstAttempt = !quizAttemptRepository.existsByStudentIdAndQuizId(studentId, quizId);

        Map<String, Integer> submitted = request.getAnswers();
        int score = 0;
        List<QuestionResultDto> results = new ArrayList<>();
        List<String> incorrectIds = new ArrayList<>();

        for (Question q : quiz.getQuestions()) {
            Integer selected = submitted.get(q.getId());
            boolean correct = selected != null && selected.equals(q.getCorrectOptionIndex());
            if (correct) {
                score++;
            } else {
                incorrectIds.add(q.getId());
            }
            results.add(QuestionResultDto.builder()
                    .questionId(q.getId())
                    .text(q.getText())
                    .yourAnswerIndex(selected)
                    .correctAnswerIndex(q.getCorrectOptionIndex())
                    .correct(correct)
                    .explanation(q.getExplanation())
                    .build());
        }

        int totalQuestions = quiz.getQuestions().size();
        int coinsEarned = isFirstAttempt ? score * quiz.getCoinsPerCorrectAnswer() : 0;
        int xpEarned = isFirstAttempt ? score * quiz.getXpPerCorrectAnswer() : 0;

        int oldRank = leaderboardService.getStudentRank(studentId);

        QuizAttempt attempt = QuizAttempt.builder()
                .studentId(studentId)
                .quizId(quizId)
                .selectedAnswers(submitted)
                .incorrectQuestionIds(incorrectIds)
                .score(score)
                .totalQuestions(totalQuestions)
                .coinsEarned(coinsEarned)
                .xpEarned(xpEarned)
                .rewarded(isFirstAttempt)
                .createdAt(Instant.now())
                .build();
        QuizAttempt saved = quizAttemptRepository.save(attempt);

        if (isFirstAttempt && (coinsEarned > 0 || xpEarned > 0)) {
            ecoCoinService.award(studentId, coinsEarned, xpEarned, CoinTransactionType.QUIZ,
                    "Quiz: " + quiz.getTitle(), saved.getId());
            gamificationService.recordActivity(studentId);
            gamificationService.checkAndAwardBadges(studentId);
            realtimeLeaderboardService.notifyIfRankChanged(studentId, oldRank);
        }

        return QuizAttemptResultResponse.builder()
                .score(score)
                .totalQuestions(totalQuestions)
                .coinsEarned(coinsEarned)
                .xpEarned(xpEarned)
                .rewarded(isFirstAttempt)
                .results(results)
                .build();
    }

    // ---------- Admin ----------

    public List<QuizAdminResponse> listQuizzesAdmin(String topicId) {
        List<Quiz> quizzes = topicId != null ? quizRepository.findByTopicId(topicId) : quizRepository.findAll();
        return quizzes.stream().map(this::toAdminResponse).toList();
    }

    public QuizAdminResponse createQuiz(QuizAdminRequest request) {
        List<Question> questions = toEntityQuestions(request.getQuestions());
        Quiz quiz = Quiz.builder()
                .topicId(request.getTopicId())
                .title(request.getTitle())
                .questions(questions)
                .coinsPerCorrectAnswer(request.getCoinsPerCorrectAnswer())
                .xpPerCorrectAnswer(request.getXpPerCorrectAnswer())
                .published(request.isPublished())
                .createdAt(Instant.now())
                .build();
        return toAdminResponse(quizRepository.save(quiz));
    }

    public QuizAdminResponse updateQuiz(String id, QuizAdminRequest request) {
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> ApiException.notFound("Quiz not found"));
        quiz.setTopicId(request.getTopicId());
        quiz.setTitle(request.getTitle());
        quiz.setQuestions(toEntityQuestions(request.getQuestions()));
        quiz.setCoinsPerCorrectAnswer(request.getCoinsPerCorrectAnswer());
        quiz.setXpPerCorrectAnswer(request.getXpPerCorrectAnswer());
        quiz.setPublished(request.isPublished());
        return toAdminResponse(quizRepository.save(quiz));
    }

    public void deleteQuiz(String id) {
        if (!quizRepository.existsById(id)) {
            throw ApiException.notFound("Quiz not found");
        }
        quizRepository.deleteById(id);
    }

    private List<Question> toEntityQuestions(List<QuestionAdminDto> dtos) {
        List<Question> questions = new ArrayList<>();
        int i = 1;
        for (QuestionAdminDto dto : dtos) {
            if (dto.getCorrectOptionIndex() < 0 || dto.getCorrectOptionIndex() > 3) {
                throw ApiException.badRequest("correctOptionIndex must be between 0 and 3");
            }
            String id = (dto.getId() == null || dto.getId().isBlank()) ? "q" + i : dto.getId();
            questions.add(new Question(id, dto.getText(), dto.getOptions(), dto.getCorrectOptionIndex(), dto.getExplanation()));
            i++;
        }
        return questions;
    }

    private QuizAdminResponse toAdminResponse(Quiz quiz) {
        List<QuestionAdminDto> questions = quiz.getQuestions().stream().map(q -> {
            QuestionAdminDto dto = new QuestionAdminDto();
            dto.setId(q.getId());
            dto.setText(q.getText());
            dto.setOptions(q.getOptions());
            dto.setCorrectOptionIndex(q.getCorrectOptionIndex());
            dto.setExplanation(q.getExplanation());
            return dto;
        }).toList();

        return QuizAdminResponse.builder()
                .id(quiz.getId())
                .topicId(quiz.getTopicId())
                .title(quiz.getTitle())
                .questions(questions)
                .coinsPerCorrectAnswer(quiz.getCoinsPerCorrectAnswer())
                .xpPerCorrectAnswer(quiz.getXpPerCorrectAnswer())
                .published(quiz.isPublished())
                .createdAt(quiz.getCreatedAt())
                .build();
    }
}
