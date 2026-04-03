package com.crackview.repository;

import com.crackview.model.StudyPlan;
import com.crackview.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class StudyPlanRepositoryTest {

    @Autowired
    private StudyPlanRepository planRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        planRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .username("planner").email("plan@test.com").build());

        planRepository.save(StudyPlan.builder()
                .user(user)
                .targetCompany("Google")
                .planData("{\"week1\": [\"Redis基础\", \"Redis持久化\"]}")
                .status("active")
                .build());

        planRepository.save(StudyPlan.builder()
                .user(user)
                .targetCompany("Meta")
                .planData("{\"week1\": [\"系统设计-缓存\"]}")
                .status("archived")
                .build());
    }

    @Test
    @DisplayName("save - should persist plan with auto-generated id and uuid")
    void save_shouldPersist() {
        StudyPlan plan = planRepository.save(StudyPlan.builder()
                .user(user)
                .targetCompany("Amazon")
                .planData("{\"week1\": [\"分布式系统\"]}")
                .build());

        assertThat(plan.getId()).isNotNull();
        assertThat(plan.getUuid()).isNotNull().hasSize(36);
        assertThat(plan.getStatus()).isEqualTo("active");
        assertThat(plan.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findByUserId - should return all plans for a user")
    void findByUserId_shouldReturnAll() {
        List<StudyPlan> plans = planRepository.findByUserId(user.getId());
        assertThat(plans).hasSize(2);
    }

    @Test
    @DisplayName("findByUserIdAndStatus - should filter by status")
    void findByUserIdAndStatus_shouldFilter() {
        List<StudyPlan> activePlans = planRepository
                .findByUserIdAndStatus(user.getId(), "active");
        assertThat(activePlans).hasSize(1);
        assertThat(activePlans.get(0).getTargetCompany()).isEqualTo("Google");
    }

    @Test
    @DisplayName("findFirstByUserIdAndStatusOrderByCreatedAtDesc - should return most recent active plan")
    void findLatestActivePlan() {
        Optional<StudyPlan> latest = planRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), "active");
        assertThat(latest).isPresent();
        assertThat(latest.get().getTargetCompany()).isEqualTo("Google");
    }

    @Test
    @DisplayName("findByUuid - should return plan when uuid exists")
    void findByUuid_shouldReturn() {
        List<StudyPlan> all = planRepository.findByUserId(user.getId());
        String uuid = all.get(0).getUuid();

        Optional<StudyPlan> found = planRepository.findByUuid(uuid);
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("findByUuid - should return empty when uuid not found")
    void findByUuid_shouldReturnEmpty_whenNotFound() {
        Optional<StudyPlan> found = planRepository.findByUuid("non-existent");
        assertThat(found).isEmpty();
    }
}
