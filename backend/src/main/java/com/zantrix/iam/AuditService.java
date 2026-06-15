package com.zantrix.iam;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

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
