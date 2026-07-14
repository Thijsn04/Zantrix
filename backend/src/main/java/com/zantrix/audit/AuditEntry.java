package com.zantrix.audit;

/**
 * A request to record one audit entry.
 *
 * <p>Callers supply the semantic fields. The audit module fills in the actor,
 * source address, timestamp, and hash chain. Only identifiers, codes, and
 * references belong here, never names or clinical content.
 *
 * @param action         what kind of action occurred
 * @param entityType     the FHIR resource type acted on, for example "Patient"
 * @param entityId       the id of the resource acted on
 * @param patientId      the patient the action concerns, if any
 * @param outcome        whether the action succeeded
 * @param breakTheGlass  whether the action used emergency access
 */
public record AuditEntry(
        AuditAction action,
        String entityType,
        String entityId,
        String patientId,
        AuditOutcome outcome,
        boolean breakTheGlass) {

    public static AuditEntry success(AuditAction action, String entityType, String entityId, String patientId) {
        return new AuditEntry(action, entityType, entityId, patientId, AuditOutcome.SUCCESS, false);
    }
}
