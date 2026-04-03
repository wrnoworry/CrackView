package com.crackview.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "interview_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    @Builder.Default
    private String uuid = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 100)
    @Column(name = "target_company", length = 100)
    private String targetCompany;

    @Size(max = 50)
    @Column(name = "domain", length = 50)
    private String domain;

    @Size(max = 20)
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "active";

    @Column(name = "total_score")
    private Float totalScore;

    @Column(name = "started_at", updatable = false)
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) uuid = UUID.randomUUID().toString();
        if (startedAt == null) startedAt = LocalDateTime.now();
        if (status == null) status = "active";
    }
}
