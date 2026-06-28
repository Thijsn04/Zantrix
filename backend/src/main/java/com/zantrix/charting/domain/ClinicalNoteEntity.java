package com.zantrix.charting.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PreUpdate;
import java.util.UUID;
import java.time.OffsetDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "clinical_note")
public class ClinicalNoteEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @Column(nullable = false)
    private UUID patientId;
    
    private UUID encounterId;
    
    @Column(nullable = false)
    private UUID authorId;
    
    private UUID supervisorId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoteStatus status = NoteStatus.IN_PROGRESS;
    
    private String noteType;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String content; // JSONB representation (TipTap)
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String fhirData;
    
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime signedAt;

    protected ClinicalNoteEntity() {}

    public ClinicalNoteEntity(UUID patientId, UUID encounterId, UUID authorId, String noteType, String content) {
        this.patientId = patientId;
        this.encounterId = encounterId;
        this.authorId = authorId;
        this.noteType = noteType;
        this.content = content;
    }

    @PreUpdate
    public void preUpdate() {
        if (this.status == NoteStatus.FINAL || this.status == NoteStatus.AMENDED) {
            // Can only change to ENTERED_IN_ERROR
            // Note: in a real environment we might use Hibernate listeners to check the *old* state
            // But for MVP, if it's already FINAL, we shouldn't allow changing content.
            // A more robust check is done in the Service layer.
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getEncounterId() { return encounterId; }
    public void setEncounterId(UUID encounterId) { this.encounterId = encounterId; }
    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }
    public UUID getSupervisorId() { return supervisorId; }
    public void setSupervisorId(UUID supervisorId) { this.supervisorId = supervisorId; }
    public NoteStatus getStatus() { return status; }
    public void setStatus(NoteStatus status) { this.status = status; }
    public String getNoteType() { return noteType; }
    public void setNoteType(String noteType) { this.noteType = noteType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getFhirData() { return fhirData; }
    public void setFhirData(String fhirData) { this.fhirData = fhirData; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(OffsetDateTime signedAt) { this.signedAt = signedAt; }
}
