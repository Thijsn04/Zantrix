package com.zantrix.cds.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cds_alert")
public class CdsAlertEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID patientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private boolean dismissed = false;

    private OffsetDateTime dismissedAt;
    private UUID dismissedBy;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected CdsAlertEntity() {}

    public CdsAlertEntity(UUID patientId, AlertSeverity severity, String title, String message) {
        this.patientId = patientId;
        this.severity = severity;
        this.title = title;
        this.message = message;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isDismissed() { return dismissed; }
    public void setDismissed(boolean dismissed) { this.dismissed = dismissed; }
    public OffsetDateTime getDismissedAt() { return dismissedAt; }
    public void setDismissedAt(OffsetDateTime dismissedAt) { this.dismissedAt = dismissedAt; }
    public UUID getDismissedBy() { return dismissedBy; }
    public void setDismissedBy(UUID dismissedBy) { this.dismissedBy = dismissedBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
