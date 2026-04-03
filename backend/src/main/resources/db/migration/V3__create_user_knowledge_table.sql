CREATE TABLE user_knowledge (
    user_id BIGINT NOT NULL,
    node_id BIGINT NOT NULL,
    score FLOAT DEFAULT 0,
    ease_factor FLOAT DEFAULT 2.5,
    interval_days INT DEFAULT 1,
    next_review_date DATE,
    last_reviewed_at TIMESTAMP NULL,
    review_count INT DEFAULT 0,
    PRIMARY KEY (user_id, node_id),
    CONSTRAINT fk_uk_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_uk_node FOREIGN KEY (node_id) REFERENCES knowledge_nodes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_uk_user ON user_knowledge(user_id);
CREATE INDEX idx_uk_node ON user_knowledge(node_id);
CREATE INDEX idx_uk_review_date ON user_knowledge(next_review_date);
