package com.zantrix.scheduling.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "waitlist")
public class WaitlistEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @Column(nullable = false)
    private UUID patientId;
    
    private String department;
    
    private String priority; // URGENT, ROUTINE
    
    private OffsetDateTime addedAt = OffsetDateTime.now();
    
    private String status; // ACTIVE, SCHEDULED, CANCELLED

    protected WaitlistEntity() {}

    public WaitlistEntity(UUID patientId, String department, String priority, String status) {
        this.patientId = patientId;
        this.department = department;
        this.priority = priority;
        this.status = status;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public OffsetDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(OffsetDateTime addedAt) { this.addedAt = addedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
