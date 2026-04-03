CREATE TABLE knowledge_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    domain VARCHAR(50) NOT NULL,
    parent_id BIGINT,
    depth INT DEFAULT 0,
    description TEXT,
    CONSTRAINT fk_kn_parent FOREIGN KEY (parent_id) REFERENCES knowledge_nodes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_kn_domain ON knowledge_nodes(domain);
CREATE INDEX idx_kn_parent ON knowledge_nodes(parent_id);
CREATE INDEX idx_kn_uuid ON knowledge_nodes(uuid);

CREATE TABLE knowledge_edges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL UNIQUE,
    source_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    relation_type VARCHAR(50) NOT NULL,
    weight FLOAT DEFAULT 1.0,
    CONSTRAINT fk_ke_source FOREIGN KEY (source_id) REFERENCES knowledge_nodes(id),
    CONSTRAINT fk_ke_target FOREIGN KEY (target_id) REFERENCES knowledge_nodes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ke_source ON knowledge_edges(source_id);
CREATE INDEX idx_ke_target ON knowledge_edges(target_id);
CREATE INDEX idx_ke_relation ON knowledge_edges(relation_type);
CREATE INDEX idx_ke_uuid ON knowledge_edges(uuid);
