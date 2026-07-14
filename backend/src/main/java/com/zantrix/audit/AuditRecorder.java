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
}
