package com.zantrix.cleaning.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cleaning_task")
public class CleaningTaskEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @Column(nullable = false)
    private UUID locationId; // The room/bed to clean
    
    private String type; // ROUTINE, DEEP_CLEAN
    
    private String status; // PENDING, IN_PROGRESS, COMPLETED
    
    private OffsetDateTime requestedAt = OffsetDateTime.now();
    
    private OffsetDateTime completedAt;

    protected CleaningTaskEntity() {}

    public CleaningTaskEntity(UUID locationId, String type, String status) {
        this.locationId = locationId;
        this.type = type;
        this.status = status;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(OffsetDateTime requestedAt) { this.requestedAt = requestedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
}
