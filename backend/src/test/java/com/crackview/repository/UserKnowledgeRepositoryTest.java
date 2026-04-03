package com.crackview.repository;

import com.crackview.model.KnowledgeNode;
import com.crackview.model.User;
import com.crackview.model.UserKnowledge;
import com.crackview.model.UserKnowledgeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserKnowledgeRepositoryTest {

    @Autowired
    private UserKnowledgeRepository userKnowledgeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KnowledgeNodeRepository nodeRepository;

    private User user;
    private KnowledgeNode nodeA;
    private KnowledgeNode nodeB;

    @BeforeEach
    void setUp() {
        userKnowledgeRepository.deleteAll();
        nodeRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .username("learner").email("learner@test.com").build());

        nodeA = nodeRepository.save(KnowledgeNode.builder()
                .name("HashMap").domain("java").depth(2).build());

        nodeB = nodeRepository.save(KnowledgeNode.builder()
                .name("Redis").domain("database").depth(1).build());

        userKnowledgeRepository.save(UserKnowledge.builder()
                .userId(user.getId()).nodeId(nodeA.getId())
                .score(80f).easeFactor(2.5f).intervalDays(3)
                .nextReviewDate(LocalDate.now().plusDays(3))
                .reviewCount(5).build());

        userKnowledgeRepository.save(UserKnowledge.builder()
                .userId(user.getId()).nodeId(nodeB.getId())
                .score(30f).easeFactor(1.8f).intervalDays(1)
                .nextReviewDate(LocalDate.now().minusDays(1))
                .reviewCount(2).build());
    }

    @Test
    @DisplayName("findById - should find by composite key (userId, nodeId)")
    void findById_compositeKey() {
        Optional<UserKnowledge> found = userKnowledgeRepository
                .findById(new UserKnowledgeId(user.getId(), nodeA.getId()));
        assertThat(found).isPresent();
        assertThat(found.get().getScore()).isEqualTo(80f);
    }

    @Test
    @DisplayName("findByUserId - should return all knowledge records for a user")
    void findByUserId_shouldReturnAll() {
        List<UserKnowledge> records = userKnowledgeRepository.findByUserId(user.getId());
        assertThat(records).hasSize(2);
    }

    @Test
    @DisplayName("findByNodeId - should return all users' records for a node")
    void findByNodeId_shouldReturnRecords() {
        List<UserKnowledge> records = userKnowledgeRepository.findByNodeId(nodeA.getId());
        assertThat(records).hasSize(1);
    }

    @Test
    @DisplayName("findByUserIdAndNextReviewDateBefore - should return overdue review items")
    void findOverdueReviews() {
        List<UserKnowledge> overdue = userKnowledgeRepository
                .findByUserIdAndNextReviewDateBefore(user.getId(), LocalDate.now());
        assertThat(overdue).hasSize(1);
        assertThat(overdue.get(0).getNodeId()).isEqualTo(nodeB.getId());
    }

    @Test
    @DisplayName("findByUserIdAndScoreLessThan - should return weak spots")
    void findWeakSpots() {
        List<UserKnowledge> weak = userKnowledgeRepository
                .findByUserIdAndScoreLessThan(user.getId(), 50f);
        assertThat(weak).hasSize(1);
        assertThat(weak.get(0).getScore()).isEqualTo(30f);
    }

    @Test
    @DisplayName("findByUserIdAndNextReviewDate - should return items due on exact date")
    void findByExactReviewDate() {
        List<UserKnowledge> todayReview = userKnowledgeRepository
                .findByUserIdAndNextReviewDate(user.getId(), LocalDate.now().minusDays(1));
        assertThat(todayReview).hasSize(1);
    }
}
