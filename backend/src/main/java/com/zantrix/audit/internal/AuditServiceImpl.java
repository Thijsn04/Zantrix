package com.zantrix.audit.internal;

import com.zantrix.audit.AuditEntry;
import com.zantrix.audit.AuditContext;
import com.zantrix.audit.AuditRecorder;
import com.zantrix.audit.AuditTrailVerifier;
import com.zantrix.audit.AuditVerificationResult;
import com.zantrix.audit.AuditQuery;
import com.zantrix.audit.AuditRecord;
import com.zantrix.audit.AuditExportSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
class AuditServiceImpl implements AuditRecorder, AuditTrailVerifier, AuditQuery, AuditExportSource {

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
        recordInternal(entry, currentActor(), currentSourceIp());
    }

    @Override
    @Transactional
    public void record(AuditEntry entry, AuditContext context) {
        recordInternal(entry, context.actor(), context.sourceIp());
    }

    private void recordInternal(AuditEntry entry, String actor, String sourceIp) {
        Instant recordedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
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
    @Transactional
    public AuditVerificationResult verify() {
        AuditChainHeadEntity head = chainHead.findByIdForVerification(HEAD_ID)
                .orElseThrow(() -> new IllegalStateException("Audit chain head is not initialized"));
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
        if (!previousHash.equals(head.getLastHash())) {
            return AuditVerificationResult.headMismatch(checked);
        }
        return AuditVerificationResult.ok(checked);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditRecord> search(String patientId,String actor,String outcome,Boolean breakTheGlass,
                                    Instant from,Instant to,int page,int size){
        if(page<0||size<1||size>500)throw new IllegalArgumentException("Audit page size must be between 1 and 500");
        return events.search(blankToNull(patientId),blankToNull(actor),blankToNull(outcome),breakTheGlass,from,to,
                PageRequest.of(page,size)).map(AuditServiceImpl::view);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditRecord> pendingExport(int limit){
        if(limit<1||limit>100)throw new IllegalArgumentException("Audit export limit must be between 1 and 100");
        return events.findTop100ByExportStatusOrderByIdAsc("PENDING").stream().limit(limit)
                .map(AuditServiceImpl::view).toList();
    }

    @Override
    @Transactional
    public void markExported(long id,String fhirAuditEventId){
        events.findById(id).orElseThrow(()->new IllegalArgumentException("Audit record not found"))
                .markExported(fhirAuditEventId);
    }

    private static AuditRecord view(AuditEventEntity value){return new AuditRecord(value.getId(),value.getRecordedAt(),
            value.getActor(),value.getAction(),value.getEntityType(),value.getEntityId(),value.getPatientId(),
            value.getOutcome(),value.getSourceIp(),value.isBreakTheGlass(),value.getExportStatus(),value.getFhirAuditEventId());}
    private static String blankToNull(String value){return value==null||value.isBlank()?null:value;}

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
