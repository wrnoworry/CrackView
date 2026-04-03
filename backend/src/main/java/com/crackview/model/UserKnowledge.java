package com.crackview.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_knowledge")
@IdClass(UserKnowledgeId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserKnowledge {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "node_id")
    private Long nodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", insertable = false, updatable = false)
    private KnowledgeNode node;

    @Column(name = "score")
    @Builder.Default
    private Float score = 0f;

    @Column(name = "ease_factor")
    @Builder.Default
    private Float easeFactor = 2.5f;

    @Column(name = "interval_days")
    @Builder.Default
    private Integer intervalDays = 1;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;
}
