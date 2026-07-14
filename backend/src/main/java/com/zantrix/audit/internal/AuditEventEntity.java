package com.zantrix.audit.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * An append only audit record. Holds identifiers, codes, and references only,
 * never names or clinical content. Each record carries the hash of its own
 * content and the chain hash that links it to the previous record.
 */
@Entity
@Table(name = "audit_event")
class AuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "patient_id")
    private String patientId;

    @Column(nullable = false)
    private String outcome;

    @Column(name = "source_ip")
    private String sourceIp;

    @Column(name = "break_the_glass", nullable = false)
    private boolean breakTheGlass;

    @Column(name = "content_hash", nullable = false)
    private String contentHash;

    @Column(name = "previous_hash", nullable = false)
    private String previousHash;

    @Column(nullable = false)
    private String hash;

    protected AuditEventEntity() {
    }

    AuditEventEntity(Instant recordedAt, String actor, String action, String entityType, String entityId,
                     String patientId, String outcome, String sourceIp, boolean breakTheGlass,
                     String contentHash, String previousHash, String hash) {
        this.recordedAt = recordedAt;
        this.actor = actor;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.patientId = patientId;
        this.outcome = outcome;
        this.sourceIp = sourceIp;
        this.breakTheGlass = breakTheGlass;
        this.contentHash = contentHash;
        this.previousHash = previousHash;
        this.hash = hash;
    }

    Long getId() {
        return id;
    }

    Instant getRecordedAt() {
        return recordedAt;
    }

    String getActor() {
        return actor;
    }

    String getAction() {
        return action;
    }

    String getEntityType() {
        return entityType;
    }

    String getEntityId() {
        return entityId;
    }

    String getPatientId() {
        return patientId;
    }

    String getOutcome() {
        return outcome;
    }

    String getSourceIp() {
        return sourceIp;
    }

    boolean isBreakTheGlass() {
        return breakTheGlass;
    }

    String getContentHash() {
        return contentHash;
    }

    String getPreviousHash() {
        return previousHash;
    }

    String getHash() {
        return hash;
    }
}
