package com.zantrix.audit.internal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Deterministic hashing for the audit chain. The same inputs always produce the
 * same hash, so the chain can be recomputed and verified.
 */
final class AuditHashing {

    static final String GENESIS = "GENESIS";

    private AuditHashing() {
    }

    /**
     * Hash of one entry's content. Timestamps are hashed at millisecond
     * precision so the value round trips through the database unchanged.
     */
    static String contentHash(Instant recordedAt, String actor, String action, String entityType,
                              String entityId, String patientId, String outcome, String sourceIp,
                              boolean breakTheGlass) {
        String canonical = String.join("|",
                Long.toString(recordedAt.toEpochMilli()),
                nullSafe(actor),
                nullSafe(action),
                nullSafe(entityType),
                nullSafe(entityId),
                nullSafe(patientId),
                nullSafe(outcome),
                nullSafe(sourceIp),
                Boolean.toString(breakTheGlass));
        return sha256(canonical);
    }

    /** Links an entry's content hash to the previous entry's chain hash. */
    static String chainHash(String previousHash, String contentHash) {
        return sha256(previousHash + "|" + contentHash);
    }

    static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static String nullSafe(String value) {
        return value == null ? "-" : value;
    }
}
