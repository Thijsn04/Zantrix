package com.zantrix.cpoe.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.UUID;
import java.time.OffsetDateTime;

@Entity(name = "CpoeServiceRequestEntity")
@Table(name = "service_request")
public class ServiceRequestEntity {

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
    private String code; // LOINC for lab, SNOMED for procedures

    private String reasonCode;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime signedAt;

    protected ServiceRequestEntity() {}

    public ServiceRequestEntity(UUID patientId, UUID requesterId, String code, String reasonCode) {
        this.patientId = patientId;
        this.requesterId = requesterId;
        this.code = code;
        this.reasonCode = reasonCode;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getRequesterId() { return requesterId; }
    public void setRequesterId(UUID requesterId) { this.requesterId = requesterId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(OffsetDateTime signedAt) { this.signedAt = signedAt; }
}
