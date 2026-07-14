package com.zantrix.audit;

/**
 * Whether the audited action succeeded, aligned with the FHIR AuditEvent.outcome
 * codes (0 for success, 8 for a serious failure).
 */
public enum AuditOutcome {

    SUCCESS("0"),
    FAILURE("8");

    private final String fhirCode;

    AuditOutcome(String fhirCode) {
        this.fhirCode = fhirCode;
    }

    public String fhirCode() {
        return fhirCode;
    }
}
