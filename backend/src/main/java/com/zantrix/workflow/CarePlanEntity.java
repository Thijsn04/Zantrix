package com.zantrix.workflow;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "care_plan")
public class CarePlanEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @Column(nullable = false)
    private UUID patientId;
    
    private String title;
    
    private String status; // DRAFT, ACTIVE, COMPLETED, REVOKED
    
    private OffsetDateTime createdDate = OffsetDateTime.now();

    protected CarePlanEntity() {}

    public CarePlanEntity(UUID patientId, String title, String status) {
        this.patientId = patientId;
        this.title = title;
        this.status = status;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(OffsetDateTime createdDate) { this.createdDate = createdDate; }
}
