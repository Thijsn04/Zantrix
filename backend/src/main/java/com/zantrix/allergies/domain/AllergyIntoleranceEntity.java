package com.zantrix.allergies.domain;

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
@Table(name = "allergy_intolerance")
public class AllergyIntoleranceEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID patientId;

    @Column(nullable = false)
    private UUID recorderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllergyType type = AllergyType.ALLERGY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Criticality criticality = Criticality.UNABLE_TO_ASSESS;

    @Column(nullable = false)
    private String code; // Z-Index ATC code or SNOMED

    @Column(nullable = false)
    private String display; // e.g., "Penicilline"

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String reactions; // JSON array of reactions

    private String note;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected AllergyIntoleranceEntity() {}

    public AllergyIntoleranceEntity(UUID patientId, UUID recorderId, String code, String display) {
        this.patientId = patientId;
        this.recorderId = recorderId;
        this.code = code;
        this.display = display;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getRecorderId() { return recorderId; }
    public void setRecorderId(UUID recorderId) { this.recorderId = recorderId; }
    public AllergyType getType() { return type; }
    public void setType(AllergyType type) { this.type = type; }
    public Criticality getCriticality() { return criticality; }
    public void setCriticality(Criticality criticality) { this.criticality = criticality; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDisplay() { return display; }
    public void setDisplay(String display) { this.display = display; }
    public String getReactions() { return reactions; }
    public void setReactions(String reactions) { this.reactions = reactions; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
