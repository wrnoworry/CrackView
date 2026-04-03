CREATE TABLE study_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    target_company VARCHAR(100),
    plan_data JSON NOT NULL,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sp_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sp_uuid ON study_plans(uuid);
CREATE INDEX idx_sp_user ON study_plans(user_id);
CREATE INDEX idx_sp_status ON study_plans(status);
