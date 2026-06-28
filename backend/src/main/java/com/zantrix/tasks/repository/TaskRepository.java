package com.zantrix.tasks.repository;

import com.zantrix.tasks.domain.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository("tasksTaskRepository")
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    List<TaskEntity> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    List<TaskEntity> findByAssigneeIdOrderByDueDateAsc(UUID assigneeId);
}
