package com.zantrix.iam;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String action;
    private String resource;
    private LocalDateTime timestamp;
    private boolean isBreakTheGlass;

    private String previousHash;
    private String hash;
    private String ipAddress;
    private String patientId;

    // Constructors
    public AuditLog() {}

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
    public void setBreakTheGlass(boolean breakTheGlass) { isBreakTheGlass = breakTheGlass; }
    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
}
