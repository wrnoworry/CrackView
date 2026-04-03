package com.crackview.repository;

import com.crackview.model.UserKnowledge;
import com.crackview.model.UserKnowledgeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserKnowledgeRepository extends JpaRepository<UserKnowledge, UserKnowledgeId> {

    List<UserKnowledge> findByUserId(Long userId);

    List<UserKnowledge> findByNodeId(Long nodeId);

    List<UserKnowledge> findByUserIdAndNextReviewDateBefore(Long userId, LocalDate date);

    List<UserKnowledge> findByUserIdAndNextReviewDate(Long userId, LocalDate date);

    List<UserKnowledge> findByUserIdAndScoreLessThan(Long userId, Float score);
}
