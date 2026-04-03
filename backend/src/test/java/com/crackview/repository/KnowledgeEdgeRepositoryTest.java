package com.crackview.repository;

import com.crackview.model.KnowledgeEdge;
import com.crackview.model.KnowledgeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class KnowledgeEdgeRepositoryTest {

    @Autowired
    private KnowledgeEdgeRepository edgeRepository;

    @Autowired
    private KnowledgeNodeRepository nodeRepository;

    private KnowledgeNode nodeA;
    private KnowledgeNode nodeB;
    private KnowledgeNode nodeC;

    @BeforeEach
    void setUp() {
        edgeRepository.deleteAll();
        nodeRepository.deleteAll();

        nodeA = nodeRepository.save(KnowledgeNode.builder()
                .name("内存模型").domain("java").depth(2).build());
        nodeB = nodeRepository.save(KnowledgeNode.builder()
                .name("GC").domain("java").depth(2).build());
        nodeC = nodeRepository.save(KnowledgeNode.builder()
                .name("Redis数据结构").domain("database").depth(2).build());

        edgeRepository.save(KnowledgeEdge.builder()
                .source(nodeA).target(nodeB)
                .relationType("prerequisite").weight(1.0f).build());

        edgeRepository.save(KnowledgeEdge.builder()
                .source(nodeA).target(nodeC)
                .relationType("related").weight(0.5f).build());
    }

    @Test
    @DisplayName("findBySourceId - should return all edges from a given source node")
    void findBySourceId_shouldReturnEdges() {
        List<KnowledgeEdge> edges = edgeRepository.findBySourceId(nodeA.getId());
        assertThat(edges).hasSize(2);
    }

    @Test
    @DisplayName("findByTargetId - should return all edges pointing to a given target node")
    void findByTargetId_shouldReturnEdges() {
        List<KnowledgeEdge> edges = edgeRepository.findByTargetId(nodeB.getId());
        assertThat(edges).hasSize(1);
        assertThat(edges.get(0).getRelationType()).isEqualTo("prerequisite");
    }

    @Test
    @DisplayName("findByRelationType - should filter edges by relation type")
    void findByRelationType_shouldFilter() {
        List<KnowledgeEdge> prereqs = edgeRepository.findByRelationType("prerequisite");
        assertThat(prereqs).hasSize(1);

        List<KnowledgeEdge> related = edgeRepository.findByRelationType("related");
        assertThat(related).hasSize(1);
    }

    @Test
    @DisplayName("findBySourceIdAndRelationType - should filter by both source and relation")
    void findBySourceIdAndRelationType_shouldFilter() {
        List<KnowledgeEdge> result = edgeRepository
                .findBySourceIdAndRelationType(nodeA.getId(), "prerequisite");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTarget().getName()).isEqualTo("GC");
    }

    @Test
    @DisplayName("save - should persist edge with auto-generated id and uuid")
    void save_shouldPersistEdge() {
        KnowledgeEdge edge = edgeRepository.save(KnowledgeEdge.builder()
                .source(nodeB).target(nodeC)
                .relationType("related").weight(0.7f).build());

        assertThat(edge.getId()).isNotNull();
        assertThat(edge.getUuid()).isNotNull().hasSize(36);
        assertThat(edge.getWeight()).isEqualTo(0.7f);
    }
}
