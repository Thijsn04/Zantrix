package com.zantrix.iam;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an immutable audit log entry.
 * <p>
 * This class captures details of critical actions performed within the system to comply
 * with MDR and NEN7510 regulations. It includes a cryptographic hash chain (blockchain-like)
 * to ensure tamper evidence and data integrity.
 * </p>
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    /**
     * The unique identifier of the audit log entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The username of the actor who performed the action. */
    private String username;
    
    /** A description of the action performed. */
    private String action;
    
    /** The resource that was accessed or modified. */
    private String resource;
    
    /** The timestamp when the action occurred. */
    private LocalDateTime timestamp;
    
    /** Indicates if the action was performed under break-the-glass (emergency) privileges. */
    private boolean isBreakTheGlass;

    /** The cryptographic hash of the preceding audit log entry, forming a chain. */
    private String previousHash;
    
    /** The cryptographic hash of this audit log entry. */
    private String hash;
    
    /** The IP address of the actor. */
    private String ipAddress;
    
    /** The identifier of the patient associated with the action, if applicable. */
    /** The identifier of the patient associated with the action, if applicable. */
    private String patientId;

    /** The type of entity being accessed or modified. */
    private String entityType;

    /** The unique identifier of the entity. */
    private String entityId;

    /** The reason code, especially for break-the-glass actions. */
    private String reasonCode;

    /** JSON Patch or diff representation of the changes. */
    @Column(columnDefinition = "text")
    private String diff;

    /**
     * Default constructor for JPA.
     */
    public AuditLog() {}

    /**
     * Constructs a new {@link AuditLog} with the specified details.
     *
     * @param username The actor's username.
     * @param action The action performed.
     * @param resource The target resource.
     * @param isBreakTheGlass Whether the action used emergency access.
     * @param ipAddress The IP address of the actor.
     * @param patientId The associated patient ID.
     */
    public AuditLog(String username, String action, String resource, boolean isBreakTheGlass, String ipAddress, String patientId) {
        this.username = username;
        this.action = action;
        this.resource = resource;
        this.timestamp = LocalDateTime.now();
        this.isBreakTheGlass = isBreakTheGlass;
        this.ipAddress = ipAddress;
        this.patientId = patientId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public boolean isBreakTheGlass() { return isBreakTheGlass; }
    public void setBreakTheGlass(boolean breakTheGlass) { this.isBreakTheGlass = breakTheGlass; }
    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public String getDiff() { return diff; }
    public void setDiff(String diff) { this.diff = diff; }
}
