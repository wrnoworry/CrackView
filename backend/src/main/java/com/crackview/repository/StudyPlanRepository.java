package com.crackview.repository;

import com.crackview.model.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    Optional<StudyPlan> findByUuid(String uuid);

    List<StudyPlan> findByUserId(Long userId);

    List<StudyPlan> findByUserIdAndStatus(Long userId, String status);

    Optional<StudyPlan> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
}
