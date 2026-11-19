package com.ecoquest.service;

import com.ecoquest.entity.Quest;
import com.ecoquest.entity.enums.QuestStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class QuestServiceTest {

    @Mock private com.ecoquest.repository.QuestRepository questRepository;
    @Mock private com.ecoquest.repository.QuestSubmissionRepository questSubmissionRepository;
    @Mock private com.ecoquest.repository.UserRepository userRepository;
    @Mock private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    @InjectMocks
    private QuestService questService;

    @Test
    void checkEligibility_activeQuestUnderLimit_returnsNull() {
        Quest quest = Quest.builder().status(QuestStatus.ACTIVE).maxSubmissionsPerStudent(2).build();
        assertThat(questService.checkEligibility(quest, 1)).isNull();
    }

    @Test
    void checkEligibility_maxSubmissionsReached_returnsReason() {
        Quest quest = Quest.builder().status(QuestStatus.ACTIVE).maxSubmissionsPerStudent(1).build();
        assertThat(questService.checkEligibility(quest, 1)).contains("Maximum submissions reached");
    }

    @Test
    void checkEligibility_inactiveQuest_returnsReason() {
        Quest quest = Quest.builder().status(QuestStatus.DRAFT).maxSubmissionsPerStudent(5).build();
        assertThat(questService.checkEligibility(quest, 0)).contains("not currently active");
    }

    @Test
    void checkEligibility_beforeStartDate_returnsReason() {
        Quest quest = Quest.builder().status(QuestStatus.ACTIVE).maxSubmissionsPerStudent(5)
                .startDate(LocalDate.now().plusDays(3)).build();
        assertThat(questService.checkEligibility(quest, 0)).contains("not started yet");
    }

    @Test
    void checkEligibility_afterEndDate_returnsReason() {
        Quest quest = Quest.builder().status(QuestStatus.ACTIVE).maxSubmissionsPerStudent(5)
                .endDate(LocalDate.now().minusDays(1)).build();
        assertThat(questService.checkEligibility(quest, 0)).contains("has ended");
    }

    @Test
    void checkEligibility_withinDateWindow_returnsNull() {
        Quest quest = Quest.builder().status(QuestStatus.ACTIVE).maxSubmissionsPerStudent(5)
                .startDate(LocalDate.now().minusDays(1)).endDate(LocalDate.now().plusDays(1)).build();
        assertThat(questService.checkEligibility(quest, 0)).isNull();
    }
}
