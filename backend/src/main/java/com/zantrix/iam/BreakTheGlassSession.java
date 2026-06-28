package com.zantrix.iam;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an active or historical break-the-glass emergency access session.
 * <p>
 * Used to track when a user requested elevated privileges for an emergency. This data is critical
 * for audits and privacy officer reviews to ensure that emergency access features are not abused,
 * aligning with MDR and NEN7510 security frameworks.
 * </p>
 */
@Entity
@Table(name = "break_the_glass_sessions")
public class BreakTheGlassSession {

    /**
     * The unique identifier for the session.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The username of the user who initiated the emergency session. */
    private String username;
    
    /** The mandatory reason provided for breaking the glass. */
    private String reason;
    
    @Column(name = "patient_id")
    private java.util.UUID patientId;

    /** The time at which the emergency access privilege expires. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    /** The time the session was created. */
    private LocalDateTime createdAt;

    /**
     * Default constructor for JPA.
     */
    public BreakTheGlassSession() {}

    /**
     * Constructs a new {@link BreakTheGlassSession}.
     *
     * @param username The username of the requester.
     * @param reason The reason for emergency access.
     * @param patientId The UUID of the patient.
     * @param expiresAt The expiration time.
     */
    public BreakTheGlassSession(String username, String reason, java.util.UUID patientId, LocalDateTime expiresAt) {
        this.username = username;
        this.reason = reason;
        this.patientId = patientId;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public void setReason(String reason) { this.reason = reason; }
    public String getReason() { return reason; }
    public java.util.UUID getPatientId() { return patientId; }
    public void setPatientId(java.util.UUID patientId) { this.patientId = patientId; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
