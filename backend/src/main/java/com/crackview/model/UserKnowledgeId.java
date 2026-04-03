package com.crackview.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserKnowledgeId implements Serializable {
    private Long userId;
    private Long nodeId;
}
