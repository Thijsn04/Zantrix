package com.zantrix.pmi.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity representing a Patient in the Patient Master Index (PMI).
 * Stores demographic and administrative data. Includes FHIR extension capabilities.
 * Adheres to MDR and NEN7510 requirements for auditability and status tracking.
 */
@Entity
@Table(name = "patient")
public class Patient {

    /** Unique identifier for the patient record. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Citizen Service Number (Burgerservicenummer). Unique across patients. */
    @Column(unique = true)
    private String bsn;

    /** Patient's first name. */
    private String firstName;

    /** Patient's last name. */
    private String lastName;
    
    /** Patient's date of birth. */
    private LocalDate birthDate;

    /** Patient's gender, aligning with FHIR administrative gender. */
    @Enumerated(EnumType.STRING)
    private Gender gender;

    /** Status of the patient record (e.g., ACTIVE, MERGED). Default is ACTIVE. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatientStatus status = PatientStatus.ACTIVE;

    /** Indicates whether the patient was created in an emergency, allowing partial data. */
    @Column(nullable = false)
    private boolean isEmergency = false;

    /** Name of the patient's insurance company. */
    private String insuranceCompany;

    /** Patient's insurance policy number. */
    private String insuranceNumber;

    /** If this record was merged, holds the ID of the target patient record. */
    private UUID mergedIntoId;

    /** Indicates if the patient is merged */
    private boolean isMerged = false;

    /** FHIR R4 Patient resource stored as JSONB for extended/custom demographic data. */
    @Convert(converter = FhirPatientConverter.class)
    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.ColumnTransformer(write = "?::jsonb")
    private org.hl7.fhir.r4.model.Patient fhirData;

    /** Timestamp indicating when the record was created. Used for audit trails (NEN7510). */
    @Column(updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Timestamp indicating the last modification of the record. */
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    /**
     * JPA lifecycle callback to update the 'updatedAt' timestamp automatically before saving changes.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters and Setters

    /** @return the unique patient ID */
    public UUID getId() { return id; }

    /** @param id the new patient ID */
    public void setId(UUID id) { this.id = id; }

    /** @return the BSN */
    public String getBsn() { return bsn; }

    /** @param bsn the new BSN */
    public void setBsn(String bsn) { this.bsn = bsn; }

    /** @return the first name */
    public String getFirstName() { return firstName; }

    /** @param firstName the new first name */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /** @return the last name */
    public String getLastName() { return lastName; }

    /** @param lastName the new last name */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /** @return the birth date */
    public LocalDate getBirthDate() { return birthDate; }

    /** @param birthDate the new birth date */
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    /** @return the gender */
    public Gender getGender() { return gender; }

    /** @param gender the new gender */
    public void setGender(Gender gender) { this.gender = gender; }

    /** @return the patient status */
    public PatientStatus getStatus() { return status; }

    /** @param status the new patient status */
    public void setStatus(PatientStatus status) { this.status = status; }

    /** @return true if it's an emergency record */
    public boolean isEmergency() { return isEmergency; }

    /** @param isEmergency true to mark as an emergency record */
    public void setEmergency(boolean isEmergency) { this.isEmergency = isEmergency; }

    /** @return the insurance company name */
    public String getInsuranceCompany() { return insuranceCompany; }

    /** @param insuranceCompany the new insurance company name */
    public void setInsuranceCompany(String insuranceCompany) { this.insuranceCompany = insuranceCompany; }

    /** @return the insurance number */
    public String getInsuranceNumber() { return insuranceNumber; }

    /** @param insuranceNumber the new insurance number */
    public void setInsuranceNumber(String insuranceNumber) { this.insuranceNumber = insuranceNumber; }

    /** @return the ID of the patient this record merged into */
    public UUID getMergedIntoId() { return mergedIntoId; }

    /** @param mergedIntoId the target patient ID */
    public void setMergedIntoId(UUID mergedIntoId) { this.mergedIntoId = mergedIntoId; }

    /** @return if patient is merged */
    public boolean isMerged() { return isMerged; }

    /** @param isMerged merge status */
    public void setMerged(boolean isMerged) { this.isMerged = isMerged; }

    /** @return the FHIR patient data */
    public org.hl7.fhir.r4.model.Patient getFhirData() { return fhirData; }

    /** @param fhirData the new FHIR patient data */
    public void setFhirData(org.hl7.fhir.r4.model.Patient fhirData) { this.fhirData = fhirData; }

    /** @return the creation timestamp */
    public OffsetDateTime getCreatedAt() { return createdAt; }

    /** @return the last update timestamp */
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
