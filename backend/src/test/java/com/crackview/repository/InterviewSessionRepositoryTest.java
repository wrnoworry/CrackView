package com.crackview.repository;

import com.crackview.model.InterviewSession;
import com.crackview.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class InterviewSessionRepositoryTest {

    @Autowired
    private InterviewSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private InterviewSession activeSession;
    private InterviewSession completedSession;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .username("interviewer").email("iv@test.com").build());

        activeSession = sessionRepository.save(InterviewSession.builder()
                .user(user)
                .targetCompany("Google")
                .domain("java")
                .status("active")
                .build());

        completedSession = sessionRepository.save(InterviewSession.builder()
                .user(user)
                .targetCompany("Meta")
                .domain("system-design")
                .status("completed")
                .totalScore(85f)
                .completedAt(LocalDateTime.now())
                .build());
    }

    @Test
    @DisplayName("save - should persist session with auto-generated id and uuid")
    void save_shouldPersist() {
        assertThat(activeSession.getId()).isNotNull();
        assertThat(activeSession.getUuid()).isNotNull().hasSize(36);
        assertThat(activeSession.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("findByUuid - should return session when uuid exists")
    void findByUuid_shouldReturn() {
        Optional<InterviewSession> found = sessionRepository.findByUuid(activeSession.getUuid());
        assertThat(found).isPresent();
        assertThat(found.get().getTargetCompany()).isEqualTo("Google");
    }

    @Test
    @DisplayName("findByUserId - should return all sessions for a user")
    void findByUserId_shouldReturnAll() {
        List<InterviewSession> sessions = sessionRepository.findByUserId(user.getId());
        assertThat(sessions).hasSize(2);
    }

    @Test
    @DisplayName("findByStatus - should filter sessions by status")
    void findByStatus_shouldFilter() {
        List<InterviewSession> active = sessionRepository.findByStatus("active");
        assertThat(active).hasSize(1);

        List<InterviewSession> completed = sessionRepository.findByStatus("completed");
        assertThat(completed).hasSize(1);
    }

    @Test
    @DisplayName("findByUserIdAndStatus - should filter by both user and status")
    void findByUserIdAndStatus_shouldFilter() {
        List<InterviewSession> result = sessionRepository
                .findByUserIdAndStatus(user.getId(), "active");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDomain()).isEqualTo("java");
    }

    @Test
    @DisplayName("findByUserIdOrderByStartedAtDesc - should return sessions in reverse chronological order")
    void findByUserIdOrdered_shouldReturnDesc() {
        List<InterviewSession> sessions = sessionRepository
                .findByUserIdOrderByStartedAtDesc(user.getId());
        assertThat(sessions).hasSize(2);
    }
}
