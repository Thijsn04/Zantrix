package com.zantrix.iam;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Service responsible for securely generating and saving audit logs.
 * <p>
 * Implements a cryptographic hash-chaining mechanism to guarantee log immutability and tamper detection.
 * This ensures strict adherence to MDR and NEN7510 requirements for secure electronic health data auditing.
 * </p>
 */
@Service
class AuditService {
    private final AuditLogRepository auditLogRepository;

    /**
     * Constructs a new {@link AuditService}.
     *
     * @param auditLogRepository The repository for audit logs.
     */
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Persists an audit log entry while linking it securely to the previous entry via SHA-256 hash.
     *
     * @param log The {@link AuditLog} entry to save.
     * @throws RuntimeException If the hashing algorithm is unavailable.
     */
    @Transactional
    public void saveSecureLog(AuditLog log) {
        String previousHash = auditLogRepository.findTopByOrderByIdDesc()
                .map(AuditLog::getHash)
                .orElse("GENESIS");
        log.setPreviousHash(previousHash);

        String dataToHash = String.join("|",
                log.getUsername(),
                log.getAction(),
                log.getResource(),
                log.getTimestamp().toString(),
                log.getIpAddress() != null ? log.getIpAddress() : "UNKNOWN",
                log.getPatientId() != null ? log.getPatientId() : "NONE",
                String.valueOf(log.isBreakTheGlass()),
                previousHash
        );

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
            log.setHash(Base64.getEncoder().encodeToString(hashBytes));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate audit hash", e);
        }

        auditLogRepository.save(log);
    }
}
