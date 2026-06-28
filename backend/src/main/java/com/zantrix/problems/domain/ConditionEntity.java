package com.zantrix.problems.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.UUID;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "condition_record")
public class ConditionEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID patientId;

    @Column(nullable = false)
    private UUID recorderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClinicalStatus clinicalStatus = ClinicalStatus.ACTIVE;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String codingSystem; // e.g., "SNOMED-CT", "ICD-10"

    @Column(nullable = false)
    private String display; // e.g., "Type 2 diabetes mellitus"

    private LocalDate onsetDate;
    private LocalDate abatementDate;
    
    private String note;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected ConditionEntity() {}

    public ConditionEntity(UUID patientId, UUID recorderId, String code, String codingSystem, String display) {
        this.patientId = patientId;
        this.recorderId = recorderId;
        this.code = code;
        this.codingSystem = codingSystem;
        this.display = display;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getRecorderId() { return recorderId; }
    public void setRecorderId(UUID recorderId) { this.recorderId = recorderId; }
    public ClinicalStatus getClinicalStatus() { return clinicalStatus; }
    public void setClinicalStatus(ClinicalStatus clinicalStatus) { this.clinicalStatus = clinicalStatus; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getCodingSystem() { return codingSystem; }
    public void setCodingSystem(String codingSystem) { this.codingSystem = codingSystem; }
    public String getDisplay() { return display; }
    public void setDisplay(String display) { this.display = display; }
    public LocalDate getOnsetDate() { return onsetDate; }
    public void setOnsetDate(LocalDate onsetDate) { this.onsetDate = onsetDate; }
    public LocalDate getAbatementDate() { return abatementDate; }
    public void setAbatementDate(LocalDate abatementDate) { this.abatementDate = abatementDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
