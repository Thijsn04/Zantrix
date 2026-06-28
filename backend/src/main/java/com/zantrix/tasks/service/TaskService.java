package com.zantrix.tasks.service;

import com.zantrix.tasks.domain.TaskEntity;
import com.zantrix.tasks.domain.TaskStatus;
import com.zantrix.tasks.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<TaskEntity> getTasksForPatient(UUID patientId) {
        return repository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public List<TaskEntity> getTasksForUser(UUID assigneeId) {
        return repository.findByAssigneeIdOrderByDueDateAsc(assigneeId);
    }

    @Transactional
    public TaskEntity createTask(TaskEntity task) {
        return repository.save(task);
    }

    @Transactional
    public TaskEntity completeTask(UUID taskId) {
        TaskEntity task = repository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setStatus(TaskStatus.DONE);
        task.setCompletedAt(OffsetDateTime.now());
        return repository.save(task);
    }
}
