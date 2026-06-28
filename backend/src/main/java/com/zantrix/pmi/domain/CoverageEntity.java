package com.zantrix.pmi.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;

@Entity
@Table(name = "coverage")
public class CoverageEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @Column(nullable = false)
    private UUID patientId;
    
    private String uzoviCode;
    
    private String policyNumber;
    
    private String status; // active, cancelled
    
    protected CoverageEntity() {}

    public CoverageEntity(UUID patientId, String uzoviCode, String policyNumber, String status) {
        this.patientId = patientId;
        this.uzoviCode = uzoviCode;
        this.policyNumber = policyNumber;
        this.status = status;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public String getUzoviCode() { return uzoviCode; }
    public void setUzoviCode(String uzoviCode) { this.uzoviCode = uzoviCode; }
    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
