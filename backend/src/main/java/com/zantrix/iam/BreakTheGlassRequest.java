package com.zantrix.iam;

/**
 * Data Transfer Object (DTO) for initiating a break-the-glass emergency session.
 * <p>
 * Carries the essential payload data, such as the obligatory reason for elevating
 * access rights. A documented reason is necessary for later review by a privacy officer.
 * </p>
 */
public class BreakTheGlassRequest {
    
    /**
     * The mandatory reason explaining why emergency access is needed.
     */
    private String reason;
    private java.util.UUID patientId;
    
    /**
     * Retrieves the reason for emergency access.
     *
     * @return The reason text.
     */
    public String getReason() { return reason; }
    
    /**
     * Sets the reason for emergency access.
     *
     * @param reason The reason text.
     */
    public void setReason(String reason) { this.reason = reason; }

    public java.util.UUID getPatientId() { return patientId; }
    public void setPatientId(java.util.UUID patientId) { this.patientId = patientId; }
}
