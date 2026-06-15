package com.zantrix.pmi.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String bsn;

    private String firstName;
    private String lastName;
    
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatientStatus status = PatientStatus.ACTIVE;

    @Column(nullable = false)
    private boolean isEmergency = false;

    private String insuranceCompany;
    private String insuranceNumber;

    private UUID mergedIntoId;

    @Convert(converter = FhirPatientConverter.class)
    @Column(columnDefinition = "jsonb")
    private org.hl7.fhir.r4.model.Patient fhirData;

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

    public String getBsn() { return bsn; }
    public void setBsn(String bsn) { this.bsn = bsn; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public PatientStatus getStatus() { return status; }
    public void setStatus(PatientStatus status) { this.status = status; }

    public boolean isEmergency() { return isEmergency; }
    public void setEmergency(boolean isEmergency) { this.isEmergency = isEmergency; }

    public String getInsuranceCompany() { return insuranceCompany; }
    public void setInsuranceCompany(String insuranceCompany) { this.insuranceCompany = insuranceCompany; }

    public String getInsuranceNumber() { return insuranceNumber; }
    public void setInsuranceNumber(String insuranceNumber) { this.insuranceNumber = insuranceNumber; }

    public UUID getMergedIntoId() { return mergedIntoId; }
    public void setMergedIntoId(UUID mergedIntoId) { this.mergedIntoId = mergedIntoId; }

    public org.hl7.fhir.r4.model.Patient getFhirData() { return fhirData; }
    public void setFhirData(org.hl7.fhir.r4.model.Patient fhirData) { this.fhirData = fhirData; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
