package com.zantrix.cpoe.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "medication_request")
public class MedicationRequestEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID patientId;

    @Column(nullable = false)
    private UUID requesterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.DRAFT;

    @Column(nullable = false)
    private String intent = "ORDER";

    @Column(nullable = false)
    private String medicationReference; // e.g., Amoxicilline 500mg

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String dosageInstruction;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String dispenseRequest;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime signedAt;

    protected MedicationRequestEntity() {}

    public MedicationRequestEntity(UUID patientId, UUID requesterId, String medicationReference) {
        this.patientId = patientId;
        this.requesterId = requesterId;
        this.medicationReference = medicationReference;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getRequesterId() { return requesterId; }
    public void setRequesterId(UUID requesterId) { this.requesterId = requesterId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    public String getMedicationReference() { return medicationReference; }
    public void setMedicationReference(String medicationReference) { this.medicationReference = medicationReference; }
    public String getDosageInstruction() { return dosageInstruction; }
    public void setDosageInstruction(String dosageInstruction) { this.dosageInstruction = dosageInstruction; }
    public String getDispenseRequest() { return dispenseRequest; }
    public void setDispenseRequest(String dispenseRequest) { this.dispenseRequest = dispenseRequest; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(OffsetDateTime signedAt) { this.signedAt = signedAt; }
}
