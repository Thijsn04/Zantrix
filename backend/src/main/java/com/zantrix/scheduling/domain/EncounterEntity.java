package com.zantrix.scheduling.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity representing a patient Encounter (visit or admission) in the Scheduling module.
 * <p>
 * This class tracks the progression of a patient through the healthcare facility.
 * Similar to appointments, it employs a hybrid storage model where core properties
 * are queried directly and extended FHIR details are stored as JSONB. This allows
 * Zantrix to maintain strict NEN7510 compliance and FHIR interoperability simultaneously.
 * </p>
 */
@Entity
@Table(name = "encounter")
public class EncounterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID patientId;

    private UUID appointmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EncounterStatus status = EncounterStatus.PLANNED;

    private UUID locationId;

    private String bedNumber;

    /** FHIR R4 Encounter resource stored as JSONB */
    @Convert(converter = FhirEncounterConverter.class)
    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.ColumnTransformer(write = "?::jsonb")
    private org.hl7.fhir.r4.model.Encounter fhirData;

    @Column(updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters and Setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public UUID getAppointmentId() { return appointmentId; }
    public void setAppointmentId(UUID appointmentId) { this.appointmentId = appointmentId; }

    public EncounterStatus getStatus() { return status; }
    public void setStatus(EncounterStatus status) { this.status = status; }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public org.hl7.fhir.r4.model.Encounter getFhirData() { return fhirData; }
    public void setFhirData(org.hl7.fhir.r4.model.Encounter fhirData) { this.fhirData = fhirData; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
