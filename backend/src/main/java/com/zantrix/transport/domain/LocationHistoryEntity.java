package com.zantrix.transport.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "location_history")
public class LocationHistoryEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @Column(nullable = false)
    private UUID patientId;
    
    @Column(nullable = false)
    private UUID locationId;
    
    private OffsetDateTime periodStart;
    
    private OffsetDateTime periodEnd;
    
    private String status; // active, completed, entered-in-error

    protected LocationHistoryEntity() {}

    public LocationHistoryEntity(UUID patientId, UUID locationId, OffsetDateTime periodStart, OffsetDateTime periodEnd, String status) {
        this.patientId = patientId;
        this.locationId = locationId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.status = status;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }
    public OffsetDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(OffsetDateTime periodStart) { this.periodStart = periodStart; }
    public OffsetDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(OffsetDateTime periodEnd) { this.periodEnd = periodEnd; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
