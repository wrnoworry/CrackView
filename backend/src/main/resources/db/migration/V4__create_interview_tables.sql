CREATE TABLE interview_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    target_company VARCHAR(100),
    domain VARCHAR(50),
    status VARCHAR(20) DEFAULT 'active',
    total_score FLOAT,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    CONSTRAINT fk_is_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_is_uuid ON interview_sessions(uuid);
CREATE INDEX idx_is_user ON interview_sessions(user_id);
CREATE INDEX idx_is_status ON interview_sessions(status);

CREATE TABLE interview_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL UNIQUE,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    related_node_id BIGINT,
    score FLOAT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_im_session FOREIGN KEY (session_id) REFERENCES interview_sessions(id),
    CONSTRAINT fk_im_node FOREIGN KEY (related_node_id) REFERENCES knowledge_nodes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_im_uuid ON interview_messages(uuid);
CREATE INDEX idx_im_session ON interview_messages(session_id);
