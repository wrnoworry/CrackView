package com.crackview.repository;

import com.crackview.model.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    Optional<InterviewSession> findByUuid(String uuid);

    List<InterviewSession> findByUserId(Long userId);

    List<InterviewSession> findByStatus(String status);

    List<InterviewSession> findByUserIdAndStatus(Long userId, String status);

    List<InterviewSession> findByUserIdOrderByStartedAtDesc(Long userId);
}
