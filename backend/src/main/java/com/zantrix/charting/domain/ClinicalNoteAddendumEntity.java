package com.zantrix.charting.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;
import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "clinical_note_addendum")
public class ClinicalNoteAddendumEntity {

    @Id
    private UUID id = UUID.randomUUID();
    
    @Column(nullable = false)
    private UUID originalNoteId;
    
    @Column(nullable = false)
    private UUID authorId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String content;
    
    private OffsetDateTime addedAt = OffsetDateTime.now();

    protected ClinicalNoteAddendumEntity() {}

    public ClinicalNoteAddendumEntity(UUID originalNoteId, UUID authorId, String content) {
        this.originalNoteId = originalNoteId;
        this.authorId = authorId;
        this.content = content;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOriginalNoteId() { return originalNoteId; }
    public void setOriginalNoteId(UUID originalNoteId) { this.originalNoteId = originalNoteId; }
    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public OffsetDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(OffsetDateTime addedAt) { this.addedAt = addedAt; }
}
