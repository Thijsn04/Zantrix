package com.zantrix.logistics;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transport_job")
public class TransportJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID patientId;

    private UUID fromLocationId;
    private UUID toLocationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status; // PENDING, ASSIGNED, IN_TRANSIT, COMPLETED, CANCELLED

    private UUID assignedTo; // e.g. a Porter ID
    
    private OffsetDateTime requestedTime;
    
    public enum Status {
        PENDING, ASSIGNED, IN_TRANSIT, COMPLETED, CANCELLED
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getFromLocationId() { return fromLocationId; }
    public void setFromLocationId(UUID fromLocationId) { this.fromLocationId = fromLocationId; }
    public UUID getToLocationId() { return toLocationId; }
    public void setToLocationId(UUID toLocationId) { this.toLocationId = toLocationId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public UUID getAssignedTo() { return assignedTo; }
    public void setAssignedTo(UUID assignedTo) { this.assignedTo = assignedTo; }
    public OffsetDateTime getRequestedTime() { return requestedTime; }
    public void setRequestedTime(OffsetDateTime requestedTime) { this.requestedTime = requestedTime; }
}
