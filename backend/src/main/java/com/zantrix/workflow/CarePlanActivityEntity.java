package com.zantrix.workflow;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "care_plan_activity")
public class CarePlanActivityEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @Column(nullable = false)
    private UUID carePlanId;
    
    private String description;
    
    private String status; // NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELLED
    
    private OffsetDateTime dueDate;

    protected CarePlanActivityEntity() {}

    public CarePlanActivityEntity(UUID carePlanId, String description, String status, OffsetDateTime dueDate) {
        this.carePlanId = carePlanId;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCarePlanId() { return carePlanId; }
    public void setCarePlanId(UUID carePlanId) { this.carePlanId = carePlanId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getDueDate() { return dueDate; }
    public void setDueDate(OffsetDateTime dueDate) { this.dueDate = dueDate; }
}
