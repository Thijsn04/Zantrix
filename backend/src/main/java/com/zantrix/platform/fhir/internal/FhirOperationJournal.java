package com.zantrix.platform.fhir.internal;

import com.zantrix.audit.AuditContext;
import com.zantrix.audit.AuditEntry;
import com.zantrix.audit.AuditRecorder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.UUID;

@Service
class FhirOperationJournal implements FhirMutationJournal {

    private final FhirOperationJournalRepository entries;
    private final AuditRecorder auditRecorder;

    FhirOperationJournal(FhirOperationJournalRepository entries, AuditRecorder auditRecorder) {
        this.entries = entries;
        this.auditRecorder = auditRecorder;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public UUID begin(FhirOperation operation, String resourceType, String resourceId, String patientId) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        entries.save(new FhirOperationJournalEntity(id, now, currentActor(), currentSourceIp(),
                operation.name(), resourceType, resourceId, patientId));
        return id;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void remoteSucceeded(UUID id, String resolvedResourceId) {
        entry(id).remoteSucceeded(resolvedResourceId, Instant.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void audited(UUID id) {
        entry(id).audited(Instant.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void remoteFailed(UUID id, Throwable failure) {
        entry(id).remoteFailed(failure, Instant.now());
    }

    @Scheduled(fixedDelayString = "${zantrix.fhir.audit-reconciliation-interval:PT30S}")
    @Transactional
    public void reconcile() {
        for (FhirOperationJournalEntity entry : entries
                .findTop100ByStateOrderByUpdatedAtAsc(FhirJournalState.REMOTE_SUCCEEDED)) {
            try {
                auditRecorder.record(AuditEntry.success(auditAction(entry.getOperation()),
                                entry.getResourceType(), entry.getResourceId(), entry.getPatientId()),
                        new AuditContext(entry.getActor(), entry.getSourceIp()));
                entry.reconciliationAttempt(null, Instant.now());
                entry.audited(Instant.now());
            } catch (RuntimeException failure) {
                entry.reconciliationAttempt(failure, Instant.now());
            }
        }
    }

    private FhirOperationJournalEntity entry(UUID id) {
        return entries.findById(id).orElseThrow(() ->
                new IllegalStateException("FHIR operation journal entry is missing: " + id));
    }

    private static com.zantrix.audit.AuditAction auditAction(String operation) {
        return switch (FhirOperation.valueOf(operation)) {
            case CREATE -> com.zantrix.audit.AuditAction.CREATE;
            case UPDATE -> com.zantrix.audit.AuditAction.UPDATE;
            case DELETE -> com.zantrix.audit.AuditAction.DELETE;
            default -> com.zantrix.audit.AuditAction.EXECUTE;
        };
    }

    private static String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getName() != null ? authentication.getName() : "system";
    }

    private static String currentSourceIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest().getRemoteAddr();
        }
        return null;
    }
}
