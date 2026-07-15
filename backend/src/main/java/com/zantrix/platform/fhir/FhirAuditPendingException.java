package com.zantrix.platform.fhir;

import java.util.UUID;

/**
 * A remote FHIR mutation committed but its audit entry is awaiting durable
 * reconciliation. Callers must not blindly retry a create operation.
 */
public final class FhirAuditPendingException extends RuntimeException {

    private final UUID operationId;

    public FhirAuditPendingException(UUID operationId, Throwable cause) {
        super("FHIR mutation committed; audit reconciliation is pending for operation " + operationId, cause);
        this.operationId = operationId;
    }

    public UUID operationId() {
        return operationId;
    }
}
