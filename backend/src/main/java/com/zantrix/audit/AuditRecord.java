package com.zantrix.audit;

import java.time.Instant;

public record AuditRecord(long id, Instant recordedAt, String actor, String action,
                          String entityType, String entityId, String patientId,
                          String outcome, String sourceIp, boolean breakTheGlass,
                          String exportStatus, String fhirAuditEventId) { }
