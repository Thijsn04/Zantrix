package com.zantrix.audit;

/**
 * The result of verifying the audit trail hash chain.
 *
 * @param intact       true if the chain is unbroken and every hash recomputes
 * @param checkedCount how many entries were checked
 * @param brokenAtId   the id of the first entry that failed, or null if intact
 */
public record AuditVerificationResult(boolean intact, long checkedCount, Long brokenAtId) {

    public static AuditVerificationResult ok(long checkedCount) {
        return new AuditVerificationResult(true, checkedCount, null);
    }

    public static AuditVerificationResult broken(long checkedCount, long brokenAtId) {
        return new AuditVerificationResult(false, checkedCount, brokenAtId);
    }
}
