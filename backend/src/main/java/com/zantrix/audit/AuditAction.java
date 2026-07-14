package com.zantrix.audit;

/**
 * The kind of action being audited, aligned with the FHIR AuditEvent.action
 * codes (C, R, U, D, E).
 */
public enum AuditAction {

    CREATE("C"),
    READ("R"),
    UPDATE("U"),
    DELETE("D"),
    EXECUTE("E");

    private final String fhirCode;

    AuditAction(String fhirCode) {
        this.fhirCode = fhirCode;
    }

    public String fhirCode() {
        return fhirCode;
    }
}
