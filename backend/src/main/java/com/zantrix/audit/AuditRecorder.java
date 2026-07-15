package com.zantrix.audit;

/**
 * Records audit entries. This is the entry point other modules use.
 *
 * <p>Recording is append only. The implementation resolves the acting user and
 * source address from the current security and request context, and links each
 * entry into a tamper evident hash chain.
 */
public interface AuditRecorder {

    void record(AuditEntry entry);

    /**
     * Records an entry for a previously captured request context. This is used
     * by durable reconciliation after a remote FHIR mutation committed but the
     * synchronous audit write did not. Implementations must preserve the
     * original actor and source rather than attributing the event to a worker.
     */
    default void record(AuditEntry entry, AuditContext context) {
        record(entry);
    }
}
