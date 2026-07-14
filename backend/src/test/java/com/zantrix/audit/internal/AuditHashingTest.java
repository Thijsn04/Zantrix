package com.zantrix.audit.internal;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AuditHashingTest {

    private static final Instant WHEN = Instant.ofEpochMilli(1_700_000_000_000L);

    @Test
    void contentHashIsDeterministicAndFixedLength() {
        String first = AuditHashing.contentHash(WHEN, "user", "READ", "Patient", "p1", "p1", "SUCCESS", "10.0.0.1", false);
        String second = AuditHashing.contentHash(WHEN, "user", "READ", "Patient", "p1", "p1", "SUCCESS", "10.0.0.1", false);

        assertThat(first).isEqualTo(second).hasSize(64);
    }

    @Test
    void contentHashChangesWhenAnyFieldChanges() {
        String base = AuditHashing.contentHash(WHEN, "user", "READ", "Patient", "p1", "p1", "SUCCESS", "10.0.0.1", false);
        String changed = AuditHashing.contentHash(WHEN, "user", "READ", "Patient", "p2", "p1", "SUCCESS", "10.0.0.1", false);

        assertThat(base).isNotEqualTo(changed);
    }

    @Test
    void chainHashDependsOnPreviousHash() {
        String content = AuditHashing.sha256("content");

        assertThat(AuditHashing.chainHash(AuditHashing.GENESIS, content))
                .isNotEqualTo(AuditHashing.chainHash("something-else", content));
    }
}
