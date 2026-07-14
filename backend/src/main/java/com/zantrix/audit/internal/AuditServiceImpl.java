package com.zantrix.audit.internal;

import com.zantrix.audit.AuditEntry;
import com.zantrix.audit.AuditRecorder;
import com.zantrix.audit.AuditTrailVerifier;
import com.zantrix.audit.AuditVerificationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Records and verifies the audit trail.
 *
 * <p>Recording is a single INSERT plus an update of the chain head under a row
 * lock. Only audit writes are serialized, not the rest of the application, which
 * fixes the scalability and race problems of the earlier design that read the
 * whole table's tail on every write and audited every method call. The acting
 * user and source address are resolved from the current context, and no
 * protected health information is stored.
 */
@Service
class AuditServiceImpl implements AuditRecorder, AuditTrailVerifier {

    private static final Long HEAD_ID = 1L;

    private final AuditEventRepository events;
    private final AuditChainHeadRepository chainHead;

    AuditServiceImpl(AuditEventRepository events, AuditChainHeadRepository chainHead) {
        this.events = events;
        this.chainHead = chainHead;
    }

    @Override
    @Transactional
    public void record(AuditEntry entry) {
        Instant recordedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        String actor = currentActor();
        String sourceIp = currentSourceIp();
        String action = entry.action().name();
        String outcome = entry.outcome().name();

        String contentHash = AuditHashing.contentHash(recordedAt, actor, action, entry.entityType(),
                entry.entityId(), entry.patientId(), outcome, sourceIp, entry.breakTheGlass());

        AuditChainHeadEntity head = chainHead.findByIdForUpdate(HEAD_ID)
                .orElseThrow(() -> new IllegalStateException("Audit chain head is not initialized"));
        String previousHash = head.getLastHash();
        String hash = AuditHashing.chainHash(previousHash, contentHash);

        events.save(new AuditEventEntity(recordedAt, actor, action, entry.entityType(), entry.entityId(),
                entry.patientId(), outcome, sourceIp, entry.breakTheGlass(), contentHash, previousHash, hash));

        head.setLastHash(hash);
        chainHead.save(head);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditVerificationResult verify() {
        String previousHash = AuditHashing.GENESIS;
        long checked = 0;
        for (AuditEventEntity event : events.findAllByOrderByIdAsc()) {
            String contentHash = AuditHashing.contentHash(event.getRecordedAt(), event.getActor(),
                    event.getAction(), event.getEntityType(), event.getEntityId(), event.getPatientId(),
                    event.getOutcome(), event.getSourceIp(), event.isBreakTheGlass());
            String expectedHash = AuditHashing.chainHash(previousHash, contentHash);

            boolean matches = contentHash.equals(event.getContentHash())
                    && previousHash.equals(event.getPreviousHash())
                    && expectedHash.equals(event.getHash());
            if (!matches) {
                return AuditVerificationResult.broken(checked, event.getId());
            }
            previousHash = event.getHash();
            checked++;
        }
        return AuditVerificationResult.ok(checked);
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
            return authentication.getName();
        }
        return "system";
    }

    private String currentSourceIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes
                && attributes.getRequest() != null) {
            return attributes.getRequest().getRemoteAddr();
        }
        return null;
    }
}
