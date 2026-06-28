package com.zantrix.referral;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.OffsetDateTime;

@Entity(name = "ReferralServiceRequestEntity")
@Table(name = "service_request")
public class ServiceRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID patientId;

    @Column(nullable = false)
    private String requester; // In FHIR, Reference(Practitioner|Organization)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status; // FHIR status: draft | active | on-hold | revoked | completed | entered-in-error | unknown

    @Column(nullable = false)
    private String intent; // FHIR intent: proposal | plan | directive | order | original-order | reflex-order | filler-order | instance-order | option

    @Column(columnDefinition = "TEXT")
    private String reasonCode; // e.g., SNOMED CT code or description

    private OffsetDateTime authoredOn;

    public enum Status {
        DRAFT, ACTIVE, ON_HOLD, REVOKED, COMPLETED, ENTERED_IN_ERROR, UNKNOWN
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public String getRequester() { return requester; }
    public void setRequester(String requester) { this.requester = requester; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public OffsetDateTime getAuthoredOn() { return authoredOn; }
    public void setAuthoredOn(OffsetDateTime authoredOn) { this.authoredOn = authoredOn; }
}
