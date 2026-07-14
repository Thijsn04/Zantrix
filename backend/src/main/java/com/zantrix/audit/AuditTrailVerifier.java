package com.zantrix.audit;

/**
 * Verifies the integrity of the audit trail by recomputing the hash chain.
 * Used by privacy officer tooling to detect tampering.
 */
public interface AuditTrailVerifier {

    AuditVerificationResult verify();
}
