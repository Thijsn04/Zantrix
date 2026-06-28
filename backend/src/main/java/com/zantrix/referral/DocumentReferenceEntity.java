package com.zantrix.referral;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "document_reference")
public class DocumentReferenceEntity {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @Column(nullable = false)
    private UUID patientId;
    
    private UUID serviceRequestId;
    
    private String status; // current, superseded, entered-in-error
    
    private String docStatus; // preliminary, final, appended, amended
    
    private String contentType; // e.g. application/pdf
    
    @Column(columnDefinition = "text")
    private String contentUrl; // URL or base64 data depending on storage strategy
    
    private OffsetDateTime date = OffsetDateTime.now();

    protected DocumentReferenceEntity() {}

    public DocumentReferenceEntity(UUID patientId, UUID serviceRequestId, String status, String docStatus, String contentType, String contentUrl) {
        this.patientId = patientId;
        this.serviceRequestId = serviceRequestId;
        this.status = status;
        this.docStatus = docStatus;
        this.contentType = contentType;
        this.contentUrl = contentUrl;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getServiceRequestId() { return serviceRequestId; }
    public void setServiceRequestId(UUID serviceRequestId) { this.serviceRequestId = serviceRequestId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDocStatus() { return docStatus; }
    public void setDocStatus(String docStatus) { this.docStatus = docStatus; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getContentUrl() { return contentUrl; }
    public void setContentUrl(String contentUrl) { this.contentUrl = contentUrl; }
    public OffsetDateTime getDate() { return date; }
    public void setDate(OffsetDateTime date) { this.date = date; }
}
