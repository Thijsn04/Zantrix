package com.zantrix.workflow;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.OffsetDateTime;

@Entity(name = "WorkflowTaskEntity")
@Table(name = "task")
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private UUID patientId;

    private UUID assigneeId; // User who needs to execute it

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private OffsetDateTime dueDate;

    public enum Status {
        OPEN, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getAssigneeId() { return assigneeId; }
    public void setAssigneeId(UUID assigneeId) { this.assigneeId = assigneeId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public OffsetDateTime getDueDate() { return dueDate; }
    public void setDueDate(OffsetDateTime dueDate) { this.dueDate = dueDate; }
}
