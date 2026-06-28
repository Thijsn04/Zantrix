package com.zantrix.scheduling.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "slot")
public class SlotEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @Column(nullable = false)
    private UUID practitionerId;
    
    private String location;
    
    private OffsetDateTime startTime;
    
    private OffsetDateTime endTime;
    
    private String status; // FREE, BUSY, BUSY-UNAVAILABLE

    protected SlotEntity() {}

    public SlotEntity(UUID practitionerId, String location, OffsetDateTime startTime, OffsetDateTime endTime, String status) {
        this.practitionerId = practitionerId;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPractitionerId() { return practitionerId; }
    public void setPractitionerId(UUID practitionerId) { this.practitionerId = practitionerId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }
    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
