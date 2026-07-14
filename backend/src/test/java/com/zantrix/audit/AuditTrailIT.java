package com.zantrix.audit;

import com.zantrix.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the audit trail end to end: entries are recorded and chained, the
 * chain verifies as intact, and tampering with a stored record is detected.
 */
class AuditTrailIT extends IntegrationTestBase {

    @Autowired
    AuditRecorder recorder;

    @Autowired
    AuditTrailVerifier verifier;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetAuditTrail() {
        jdbcTemplate.update("delete from audit_event");
        jdbcTemplate.update("update audit_chain_head set last_hash = 'GENESIS' where id = 1");
    }

    @Test
    void recordsAChainAndDetectsTampering() {
        recorder.record(AuditEntry.success(AuditAction.CREATE, "Patient", "p1", "p1"));
        recorder.record(AuditEntry.success(AuditAction.READ, "Patient", "p1", "p1"));
        recorder.record(AuditEntry.success(AuditAction.UPDATE, "Observation", "o1", "p1"));

        AuditVerificationResult intact = verifier.verify();
        assertThat(intact.intact()).isTrue();
        assertThat(intact.checkedCount()).isEqualTo(3);

        Long firstId = jdbcTemplate.queryForObject("select min(id) from audit_event", Long.class);
        jdbcTemplate.update("update audit_event set patient_id = 'TAMPERED' where id = ?", firstId);

        AuditVerificationResult broken = verifier.verify();
        assertThat(broken.intact()).isFalse();
        assertThat(broken.brokenAtId()).isEqualTo(firstId);
    }

    @Test
    void detectsRemovalOfTheLastEntry() {
        recorder.record(AuditEntry.success(AuditAction.CREATE, "Patient", "p1", "p1"));
        recorder.record(AuditEntry.success(AuditAction.READ, "Patient", "p1", "p1"));

        jdbcTemplate.update("delete from audit_event where id = (select max(id) from audit_event)");

        AuditVerificationResult broken = verifier.verify();
        assertThat(broken.intact()).isFalse();
        assertThat(broken.checkedCount()).isEqualTo(1);
        assertThat(broken.brokenAtId()).isNull();
    }
}
