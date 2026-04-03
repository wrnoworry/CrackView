package com.crackview.repository;

import com.crackview.model.InterviewMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewMessageRepository extends JpaRepository<InterviewMessage, Long> {

    List<InterviewMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    List<InterviewMessage> findBySessionId(Long sessionId);

    List<InterviewMessage> findByRelatedNodeId(Long relatedNodeId);

    long countBySessionId(Long sessionId);
}
