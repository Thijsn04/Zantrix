package com.zantrix.tasks.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.UUID;
import java.time.OffsetDateTime;

@Entity(name = "TasksTaskEntity")
@Table(name = "clinical_task")
public class TaskEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID patientId;

    private UUID assigneeId; // User responsible for the task

    @Column(nullable = false)
    private UUID creatorId; // User who created the task

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.NORMAL;

    @Column(nullable = false)
    private String title;

    private String description;

    private OffsetDateTime dueDate;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime completedAt;

    protected TaskEntity() {}

    public TaskEntity(UUID patientId, UUID creatorId, String title) {
        this.patientId = patientId;
        this.creatorId = creatorId;
        this.title = title;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getAssigneeId() { return assigneeId; }
    public void setAssigneeId(UUID assigneeId) { this.assigneeId = assigneeId; }
    public UUID getCreatorId() { return creatorId; }
    public void setCreatorId(UUID creatorId) { this.creatorId = creatorId; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public OffsetDateTime getDueDate() { return dueDate; }
    public void setDueDate(OffsetDateTime dueDate) { this.dueDate = dueDate; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
}
