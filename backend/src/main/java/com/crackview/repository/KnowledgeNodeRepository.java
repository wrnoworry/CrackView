package com.crackview.repository;

import com.crackview.model.KnowledgeNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeNodeRepository extends JpaRepository<KnowledgeNode, Long> {

    Optional<KnowledgeNode> findByUuid(String uuid);

    List<KnowledgeNode> findByDomain(String domain);

    List<KnowledgeNode> findByParentId(Long parentId);

    List<KnowledgeNode> findByParentIsNull();

    List<KnowledgeNode> findByDepth(Integer depth);

    List<KnowledgeNode> findByDomainAndDepth(String domain, Integer depth);
}
