package com.zantrix.vitals.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "vital_sign_observation")
public class VitalSignObservationEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID patientId;

    @Column(nullable = false)
    private UUID recorderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VitalSignType type;

    @Column(nullable = false)
    private Double value;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private OffsetDateTime measuredAt;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected VitalSignObservationEntity() {}

    public VitalSignObservationEntity(UUID patientId, UUID recorderId, VitalSignType type, Double value, String unit, OffsetDateTime measuredAt) {
        this.patientId = patientId;
        this.recorderId = recorderId;
        this.type = type;
        this.value = value;
        this.unit = unit;
        this.measuredAt = measuredAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getRecorderId() { return recorderId; }
    public void setRecorderId(UUID recorderId) { this.recorderId = recorderId; }
    public VitalSignType getType() { return type; }
    public void setType(VitalSignType type) { this.type = type; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public OffsetDateTime getMeasuredAt() { return measuredAt; }
    public void setMeasuredAt(OffsetDateTime measuredAt) { this.measuredAt = measuredAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
