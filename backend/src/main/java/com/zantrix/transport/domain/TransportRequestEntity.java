package com.zantrix.transport.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transport_request")
public class TransportRequestEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @Column(nullable = false)
    private UUID patientId;
    
    private UUID originLocationId;
    
    private UUID destinationLocationId;
    
    private String status; // REQUESTED, DISPATCHED, IN_PROGRESS, COMPLETED, CANCELLED
    
    private OffsetDateTime requestedAt = OffsetDateTime.now();
    
    private OffsetDateTime scheduledFor;

    protected TransportRequestEntity() {}

    public TransportRequestEntity(UUID patientId, UUID originLocationId, UUID destinationLocationId, String status, OffsetDateTime scheduledFor) {
        this.patientId = patientId;
        this.originLocationId = originLocationId;
        this.destinationLocationId = destinationLocationId;
        this.status = status;
        this.scheduledFor = scheduledFor;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getOriginLocationId() { return originLocationId; }
    public void setOriginLocationId(UUID originLocationId) { this.originLocationId = originLocationId; }
    public UUID getDestinationLocationId() { return destinationLocationId; }
    public void setDestinationLocationId(UUID destinationLocationId) { this.destinationLocationId = destinationLocationId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(OffsetDateTime requestedAt) { this.requestedAt = requestedAt; }
    public OffsetDateTime getScheduledFor() { return scheduledFor; }
    public void setScheduledFor(OffsetDateTime scheduledFor) { this.scheduledFor = scheduledFor; }
}
