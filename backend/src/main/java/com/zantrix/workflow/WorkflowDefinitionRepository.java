package com.zantrix.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinitionEntity, UUID> {}
