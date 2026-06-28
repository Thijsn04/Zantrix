package com.zantrix.terminology.repository;

import com.zantrix.terminology.domain.ConceptMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ConceptMapRepository extends JpaRepository<ConceptMapEntity, UUID> {
    List<ConceptMapEntity> findBySourceSystemAndSourceCodeAndTargetSystem(String sourceSystem, String sourceCode, String targetSystem);
}
