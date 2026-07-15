package com.zantrix.platform.fhir.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fhir_operation_journal")
class FhirOperationJournalEntity {

    @Id
    private UUID id;
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(nullable = false)
    private String actor;
    @Column(name = "source_ip")
    private String sourceIp;
    @Column(nullable = false)
    private String operation;
    @Column(name = "resource_type", nullable = false)
    private String resourceType;
    @Column(name = "resource_id")
    private String resourceId;
    @Column(name = "patient_id")
    private String patientId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FhirJournalState state;
    @Column(name = "error_code")
    private String errorCode;
    @Column(nullable = false)
    private int attempts;

    protected FhirOperationJournalEntity() {
    }

    FhirOperationJournalEntity(UUID id, Instant now, String actor, String sourceIp,
                               String operation, String resourceType, String resourceId,
                               String patientId) {
        this.id = id;
        this.startedAt = now;
        this.updatedAt = now;
        this.actor = actor;
        this.sourceIp = sourceIp;
        this.operation = operation;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.patientId = patientId;
        this.state = FhirJournalState.PENDING;
    }

    UUID getId() { return id; }
    String getActor() { return actor; }
    String getSourceIp() { return sourceIp; }
    String getOperation() { return operation; }
    String getResourceType() { return resourceType; }
    String getResourceId() { return resourceId; }
    String getPatientId() { return patientId; }

    void remoteSucceeded(String resolvedResourceId, Instant now) {
        if (resolvedResourceId != null && !resolvedResourceId.isBlank()) {
            this.resourceId = resolvedResourceId;
        }
        this.state = FhirJournalState.REMOTE_SUCCEEDED;
        this.errorCode = null;
        this.updatedAt = now;
    }

    void audited(Instant now) {
        this.state = FhirJournalState.AUDITED;
        this.errorCode = null;
        this.updatedAt = now;
    }

    void remoteFailed(Throwable failure, Instant now) {
        this.state = FhirJournalState.REMOTE_FAILED;
        this.errorCode = failure.getClass().getSimpleName();
        this.updatedAt = now;
    }

    void reconciliationAttempt(Throwable failure, Instant now) {
        this.attempts++;
        this.errorCode = failure == null ? null : failure.getClass().getSimpleName();
        this.updatedAt = now;
    }
}
