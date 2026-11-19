package com.ecoquest.seed;

import com.ecoquest.dto.auth.RegisterRequest;
import com.ecoquest.dto.learning.QuizAttemptRequest;
import com.ecoquest.entity.*;
import com.ecoquest.entity.embedded.GeoLocation;
import com.ecoquest.entity.embedded.LocalizedText;
import com.ecoquest.entity.embedded.Question;
import com.ecoquest.entity.enums.*;
import com.ecoquest.repository.*;
import com.ecoquest.service.AuthService;
import com.ecoquest.service.QuizService;
import com.ecoquest.service.SubmissionReviewService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * Populates demo-ready data (spec #33) so the platform is immediately presentable without
 * manual setup. Gated behind SEED_DATA=true (default false) and idempotent - skips entirely if
 * any school already exists, so it never runs twice against the same database.
 *
 * Demo accounts (all password: Demo@1234):
 *   admin@ecoquest.com
 *   teacher1@ecoquest.com / teacher2@ecoquest.com / teacher3@ecoquest.com
 *   student1@ecoquest.com ... student9@ecoquest.com
 *
 * Users are created through AuthService (real bcrypt hashing); sample quiz/quest activity
 * goes through the real QuizService/SubmissionReviewService so eco-coins, XP, badges, and
 * impact metrics all end up genuinely consistent - not just inserted numbers.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEMO_PASSWORD = "Demo@1234";

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    private final SchoolRepository schoolRepository;
    private final EvsTopicRepository topicRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final QuestRepository questRepository;
    private final BadgeRepository badgeRepository;
    private final QuestSubmissionRepository submissionRepository;
    private final AuthService authService;
    private final QuizService quizService;
    private final SubmissionReviewService submissionReviewService;

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        if (schoolRepository.count() > 0) {
            log.info("Seed data already present - skipping DataSeeder");
            return;
        }

        log.info("Seeding demo data...");

        List<School> schools = seedSchools();
        Map<String, String> userIds = seedUsers(schools);
        List<EvsTopic> topics = seedTopics();
        seedLessons(topics);
        List<Quiz> quizzes = seedQuizzes(topics);
        List<Quest> quests = seedQuests(schools, userIds.get("admin"));
        seedBadges();
        seedSampleActivity(userIds, quests, quizzes);

        log.info("Seed data complete: {} schools, {} topics, {} quizzes, {} quests",
                schools.size(), topics.size(), quizzes.size(), quests.size());
    }

    // ---------- Schools ----------

    private List<School> seedSchools() {
        List<School> schools = List.of(
                School.builder().name("D.Y. Patil Vidyalaya").city("Pune").state("Maharashtra")
                        .campusLocation(new GeoLocation(18.5679, 73.9143)).createdAt(Instant.now()).build(),
                School.builder().name("Delhi Public School, R.K. Puram").city("New Delhi").state("Delhi")
                        .campusLocation(new GeoLocation(28.5672, 77.1867)).createdAt(Instant.now()).build(),
                School.builder().name("Chennai Public School").city("Chennai").state("Tamil Nadu")
                        .campusLocation(new GeoLocation(13.0827, 80.2707)).createdAt(Instant.now()).build()
        );
        return schoolRepository.saveAll(schools);
    }

    // ---------- Users ----------

    private Map<String, String> seedUsers(List<School> schools) {
        Map<String, String> ids = new HashMap<>();

        ids.put("admin", register("Admin User", "admin@ecoquest.com", Role.ADMIN, null, null, null));

        for (int i = 0; i < schools.size(); i++) {
            String schoolId = schools.get(i).getId();
            String teacherId = register("Teacher " + (i + 1), "teacher" + (i + 1) + "@ecoquest.com",
                    Role.TEACHER, schoolId, null, null);
            ids.put("teacher" + (i + 1), teacherId);

            for (int s = 1; s <= 3; s++) {
                int studentNum = i * 3 + s;
                String studentId = register("Student " + studentNum, "student" + studentNum + "@ecoquest.com",
                        Role.STUDENT, null, schoolId, "10th");
                ids.put("student" + studentNum, studentId);
            }
        }
        return ids;
    }

    private String register(String name, String email, Role role, String assignedSchoolId,
                             String studentSchoolId, String grade) {
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword(DEMO_PASSWORD);
        request.setRole(role);
        request.setAssignedSchoolId(assignedSchoolId);
        request.setSchoolId(studentSchoolId);
        request.setGrade(grade);
        return authService.register(request).getUserId();
    }

    // ---------- Topics ----------

    private List<EvsTopic> seedTopics() {
        String[][] data = {
                {"Climate Change", "Understand global warming, greenhouse gases, and how India is affected and responding.", "जलवायु परिवर्तन"},
                {"Waste Management", "Learn to segregate, reduce, and manage household and community waste responsibly.", "कचरा व्यवस्थापन"},
                {"Water Conservation", "Practical ways to save water at home, in school, and in your community.", "जल संवर्धन"},
                {"Renewable Energy", "Solar, wind, and other clean energy sources powering India's future.", "अक्षय ऊर्जा"},
                {"Biodiversity", "Why variety of life matters, and how to protect local ecosystems.", "जैवविविधता"},
                {"Air Pollution", "Sources of air pollution, health effects, and what individuals can do.", "वायु प्रदूषण"},
                {"Sustainable Transportation", "Walking, cycling, public transport, and reducing your carbon footprint on the move.", "शाश्वत वाहतूक"},
                {"Forest Conservation", "The role forests play, deforestation pressures, and conservation efforts in India.", "वन संवर्धन"},
                {"Plastic Pollution", "The lifecycle of plastic, its environmental cost, and how to reduce single-use plastic.", "प्लास्टिक प्रदूषण"},
                {"Energy Conservation", "Everyday habits and technology choices that reduce energy waste.", "ऊर्जा संवर्धन"},
        };

        List<EvsTopic> topics = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            topics.add(EvsTopic.builder()
                    .title(new LocalizedText(data[i][0], null, data[i][2]))
                    .description(new LocalizedText(data[i][1], null, null))
                    .learningObjectives(List.of(new LocalizedText("Understand the basics of " + data[i][0].toLowerCase() + " and one real action you can take.", null, null)))
                    .orderIndex(i + 1)
                    .published(true)
                    .createdAt(Instant.now())
                    .build());
        }
        return topicRepository.saveAll(topics);
    }

    // ---------- Lessons (2 per topic = 20) ----------

    private void seedLessons(List<EvsTopic> topics) {
        List<Lesson> lessons = new ArrayList<>();
        for (EvsTopic topic : topics) {
            String name = topic.getTitle().getEn();
            lessons.add(Lesson.builder()
                    .topicId(topic.getId())
                    .title(new LocalizedText("What is " + name + "?", null, null))
                    .body(new LocalizedText("A short introduction to " + name.toLowerCase()
                            + ": what it means, why it matters, and how it shows up in daily life in India.", null, null))
                    .localContextNote(new LocalizedText("Relevant Indian programs and examples are woven into class discussion for this topic.", null, null))
                    .orderIndex(1)
                    .createdAt(Instant.now())
                    .build());
            lessons.add(Lesson.builder()
                    .topicId(topic.getId())
                    .title(new LocalizedText("Taking Action on " + name, null, null))
                    .body(new LocalizedText("Simple, concrete steps a student can take this week related to " + name.toLowerCase() + ".", null, null))
                    .orderIndex(2)
                    .createdAt(Instant.now())
                    .build());
        }
        lessonRepository.saveAll(lessons);
    }

    // ---------- Quizzes (1 per topic, 3 questions each = 30) ----------

    private List<Quiz> seedQuizzes(List<EvsTopic> topics) {
        List<Quiz> quizzes = new ArrayList<>();
        for (EvsTopic topic : topics) {
            String name = topic.getTitle().getEn();
            List<Question> questions = List.of(
                    new Question("q1", "Which best describes " + name + "?",
                            List.of("A key environmental topic covered in this course", "A type of vehicle", "A programming language", "A currency"),
                            0, "This topic is one of the core EVS themes covered on EcoQuest India."),
                    new Question("q2", "True or false: individual actions can meaningfully contribute to progress on " + name.toLowerCase() + ".",
                            List.of("True", "False", "Only governments can help", "Only large companies can help"),
                            0, "Small, consistent individual actions add up, especially at community scale."),
                    new Question("q3", "What is a good first step a student can take related to " + name.toLowerCase() + "?",
                            List.of("Learn about it, then take one small real-world action", "Ignore it", "Wait for someone else to act", "Only discuss it in theory"),
                            0, "EcoQuest India is built around exactly this: learn, then act, then verify.")
            );
            quizzes.add(Quiz.builder()
                    .topicId(topic.getId())
                    .title(name + " Quiz")
                    .questions(questions)
                    .coinsPerCorrectAnswer(10)
                    .xpPerCorrectAnswer(20)
                    .published(true)
                    .createdAt(Instant.now())
                    .build());
        }
        return quizRepository.saveAll(quizzes);
    }

    // ---------- Quests (15, spanning all categories) ----------

    private List<Quest> seedQuests(List<School> schools, String adminId) {
        GeoLocation school1Location = schools.get(0).getCampusLocation();

        List<Quest> quests = List.of(
                quest("Waste Warrior", "Segregate your household waste into wet and dry categories for a week.",
                        QuestCategory.WASTE_MANAGEMENT, QuestDifficulty.EASY, 50, 30,
                        "Photo of segregated wet/dry waste bins", false, null, null, adminId),
                quest("Tree Guardian", "Plant a tree or care for an existing one in your neighborhood.",
                        QuestCategory.TREE_PLANTATION, QuestDifficulty.MEDIUM, 200, 150,
                        "Photo of the planted or cared-for tree", true, school1Location, 500, adminId),
                quest("Water Saver", "Practice water conservation at home for a week (shorter showers, fixing leaks, etc.).",
                        QuestCategory.WATER_CONSERVATION, QuestDifficulty.EASY, 50, 30,
                        "Photo showing your water-saving action", false, null, null, adminId),
                quest("Energy Saver", "Reduce unnecessary electricity usage at home for a week.",
                        QuestCategory.ENERGY_CONSERVATION, QuestDifficulty.EASY, 50, 30,
                        "Photo of switched-off appliances/meter reading", false, null, null, adminId),
                quest("Plastic-Free Day", "Avoid single-use plastic for one full day.",
                        QuestCategory.PLASTIC_FREE, QuestDifficulty.MEDIUM, 75, 50,
                        "Photo of plastic-free alternatives you used", false, null, null, adminId),
                quest("Sustainable Travel", "Use walking, cycling, or public transport instead of a private vehicle.",
                        QuestCategory.SUSTAINABLE_TRANSPORT, QuestDifficulty.EASY, 75, 50,
                        "Photo of your sustainable commute", false, null, null, adminId),
                quest("Community Cleanup Drive", "Organize or join a cleanup drive in a local public space.",
                        QuestCategory.COMMUNITY_CLEANUP, QuestDifficulty.HARD, 150, 100,
                        "Before/after photo of the cleaned area", true, school1Location, 1000, adminId),
                quest("Backyard Biodiversity Watch", "Observe and document 3 different local plant or animal species near you.",
                        QuestCategory.BIODIVERSITY, QuestDifficulty.MEDIUM, 100, 75,
                        "Photos of the species observed", false, null, null, adminId),
                quest("Rainwater Harvesting Setup", "Set up or document a simple rainwater harvesting system.",
                        QuestCategory.WATER_CONSERVATION, QuestDifficulty.HARD, 250, 150,
                        "Photo of the harvesting setup", false, null, null, adminId),
                quest("Composting at Home", "Start a compost bin for kitchen waste.",
                        QuestCategory.WASTE_MANAGEMENT, QuestDifficulty.MEDIUM, 100, 60,
                        "Photo of your compost setup", false, null, null, adminId),
                quest("Switch to LED Bulbs", "Replace at least 2 incandescent/CFL bulbs at home with LEDs.",
                        QuestCategory.ENERGY_CONSERVATION, QuestDifficulty.EASY, 40, 25,
                        "Photo of the LED bulbs installed", false, null, null, adminId),
                quest("Carpool Champion", "Carpool or share a ride instead of traveling alone, at least twice this week.",
                        QuestCategory.SUSTAINABLE_TRANSPORT, QuestDifficulty.EASY, 60, 40,
                        "Photo/note documenting the shared ride", false, null, null, adminId),
                quest("Say No to Single-Use Plastic Week", "Avoid single-use plastic for a full week, not just a day.",
                        QuestCategory.PLASTIC_FREE, QuestDifficulty.HARD, 200, 120,
                        "Daily photo log of plastic-free choices", false, null, null, adminId),
                quest("Plant a Kitchen Garden", "Start a small kitchen garden with at least 2 plant varieties.",
                        QuestCategory.BIODIVERSITY, QuestDifficulty.MEDIUM, 120, 80,
                        "Photo of your kitchen garden", false, null, null, adminId),
                quest("River or Beach Cleanup", "Join or organize a cleanup at a local river bank, lake, or beach.",
                        QuestCategory.COMMUNITY_CLEANUP, QuestDifficulty.MEDIUM, 130, 90,
                        "Before/after photo of the cleaned site", false, null, null, adminId)
        );
        return questRepository.saveAll(quests);
    }

    private Quest quest(String title, String description, QuestCategory category, QuestDifficulty difficulty,
                         int coins, int xp, String proofDescription, boolean gpsRequired,
                         GeoLocation location, Integer radius, String createdBy) {
        return Quest.builder()
                .title(title).description(description).category(category).difficulty(difficulty)
                .ecoCoinsReward(coins).xpReward(xp)
                .proofRequired(true).requiredProofDescription(proofDescription)
                .gpsRequired(gpsRequired).targetLocation(gpsRequired ? location : null)
                .allowedRadiusMeters(gpsRequired ? radius : null)
                .maxSubmissionsPerStudent(1).status(QuestStatus.ACTIVE)
                .createdByUserId(createdBy).schoolId(null)
                .createdAt(Instant.now())
                .build();
    }

    // ---------- Badges (10) ----------

    private void seedBadges() {
        List<Badge> badges = List.of(
                badge("FIRST_STEP", "First Step", "Complete your first quest.", "🌱",
                        BadgeCriteriaType.QUESTS_COMPLETED_TOTAL, 1, null, 25, 20),
                badge("WASTE_WARRIOR", "Waste Warrior", "Complete 10 waste-management quests.", "♻️",
                        BadgeCriteriaType.QUESTS_COMPLETED_IN_CATEGORY, 10, QuestCategory.WASTE_MANAGEMENT, 100, 60),
                badge("WATER_SAVER", "Water Saver", "Complete 10 water conservation quests.", "💧",
                        BadgeCriteriaType.QUESTS_COMPLETED_IN_CATEGORY, 10, QuestCategory.WATER_CONSERVATION, 100, 60),
                badge("GREEN_GUARDIAN", "Green Guardian", "Complete 5 tree-related quests.", "🌳",
                        BadgeCriteriaType.QUESTS_COMPLETED_IN_CATEGORY, 5, QuestCategory.TREE_PLANTATION, 150, 100),
                badge("ENERGY_SAVER", "Energy Saver", "Complete 10 energy conservation quests.", "⚡",
                        BadgeCriteriaType.QUESTS_COMPLETED_IN_CATEGORY, 10, QuestCategory.ENERGY_CONSERVATION, 100, 60),
                badge("CONSISTENCY_CHAMPION", "Consistency Champion", "Maintain a 7-day streak.", "🔥",
                        BadgeCriteriaType.STREAK_DAYS, 7, null, 100, 50),
                badge("ECO_CHAMPION", "Eco Champion", "Earn 5,000 Eco-Coins.", "🌍",
                        BadgeCriteriaType.ECO_COINS_EARNED_TOTAL, 5000, null, 300, 200),
                badge("PLASTIC_FIGHTER", "Plastic Fighter", "Complete 5 plastic-free quests.", "🚯",
                        BadgeCriteriaType.QUESTS_COMPLETED_IN_CATEGORY, 5, QuestCategory.PLASTIC_FREE, 80, 50),
                badge("QUIZ_MASTER", "Quiz Master", "Complete 10 quizzes.", "📚",
                        BadgeCriteriaType.QUIZZES_COMPLETED_TOTAL, 10, null, 100, 60),
                badge("COMMUNITY_CHAMPION", "Community Champion", "Complete 5 community cleanup quests.", "🤝",
                        BadgeCriteriaType.QUESTS_COMPLETED_IN_CATEGORY, 5, QuestCategory.COMMUNITY_CLEANUP, 120, 80)
        );
        badgeRepository.saveAll(badges);
    }

    private Badge badge(String code, String name, String description, String icon,
                         BadgeCriteriaType criteriaType, int threshold, QuestCategory category,
                         int coins, int xp) {
        return Badge.builder()
                .code(code).name(name).description(description).icon(icon)
                .criteriaType(criteriaType).criteriaThreshold(threshold).criteriaCategory(category)
                .ecoCoinsReward(coins).xpReward(xp)
                .createdAt(Instant.now())
                .build();
    }

    // ---------- Sample activity (real, ledger-consistent) ----------

    private void seedSampleActivity(Map<String, String> userIds, List<Quest> quests, List<Quiz> quizzes) {
        String student1 = userIds.get("student1");
        String student2 = userIds.get("student2");
        String admin = userIds.get("admin");

        // Student 1 takes the first quiz with all-correct answers - real reward via QuizService.
        Quiz firstQuiz = quizzes.get(0);
        QuizAttemptRequest attemptRequest = new QuizAttemptRequest();
        Map<String, Integer> answers = new HashMap<>();
        for (Question q : firstQuiz.getQuestions()) {
            answers.put(q.getId(), q.getCorrectOptionIndex());
        }
        attemptRequest.setAnswers(answers);
        quizService.submitAttempt(student1, firstQuiz.getId(), attemptRequest);

        // Student 1: one APPROVED "Waste Warrior" submission - real reward + impact via
        // SubmissionReviewService (imageUrl left null - no physical seed image, frontend
        // handles a missing thumbnail gracefully).
        Quest wasteWarrior = quests.get(0);
        QuestSubmission approved = submissionRepository.save(QuestSubmission.builder()
                .questId(wasteWarrior.getId()).studentId(student1)
                .description("Segregated our kitchen waste into wet and dry bins all week.")
                .status(SubmissionStatus.AI_REVIEWED)
                .rewardGranted(false).adminOverride(false)
                .createdAt(Instant.now())
                .build());
        submissionReviewService.approveAsAdmin(admin, approved.getId());

        // Student 2: one PENDING submission, sitting in the review queue for the demo.
        Quest waterSaver = quests.get(2);
        submissionRepository.save(QuestSubmission.builder()
                .questId(waterSaver.getId()).studentId(student2)
                .description("Fixed a leaking tap and started timing my showers.")
                .status(SubmissionStatus.PENDING)
                .rewardGranted(false).adminOverride(false)
                .createdAt(Instant.now())
                .build());

        log.info("Sample activity seeded for student1/student2");
    }
}
