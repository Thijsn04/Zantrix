package com.zantrix.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository("workflowTaskRepository")
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    List<TaskEntity> findByAssigneeIdAndStatus(UUID assigneeId, TaskEntity.Status status);
}
