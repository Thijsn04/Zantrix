package com.zantrix.platform.fhir.internal;

import com.zantrix.audit.AuditAction;
import com.zantrix.audit.AuditEntry;
import com.zantrix.audit.AuditOutcome;
import com.zantrix.audit.AuditRecorder;
import com.zantrix.platform.fhir.FhirAuditPendingException;
import com.zantrix.platform.security.EmergencyAccessContext;
import com.zantrix.platform.security.EmergencyAccessReview;
import com.zantrix.platform.security.EmergencyAccessReviewRecorder;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
class EmergencyAccessReviewService implements EmergencyAccessReviewRecorder {

    private static final Set<String> OUTCOMES=Set.of("appropriate","inappropriate","needs-follow-up");
    private final EmergencyAccessReviewRepository reviews;
    private final FhirTransport transport;
    private final FhirMutationJournal journal;
    private final AuditRecorder audit;
    EmergencyAccessReviewService(EmergencyAccessReviewRepository reviews,FhirTransport transport,
                                 FhirMutationJournal journal,AuditRecorder audit){
        this.reviews=reviews;this.transport=transport;this.journal=journal;this.audit=audit;
    }

    @Override
    public void record(String resourceType,String resourceId,String patientId){
        if(!EmergencyAccessContext.active()||patientId==null||patientId.isBlank())return;
        String actor=SecurityContextHolder.getContext().getAuthentication().getName();
        UUID id=UUID.randomUUID();
        EmergencyAccessReviewEntity review=new EmergencyAccessReviewEntity(id, Instant.now(),actor,
                EmergencyAccessContext.reasonCode().orElseThrow(),patientId,resourceType,resourceId);
        reviews.saveAndFlush(review);
        Task task=reviewTask(id,patientId,resourceType,resourceId);
        UUID operation=journal.begin(FhirOperation.CREATE,"Task",task.getIdElement().getIdPart(),patientId);
        try {
            transport.create(task);
            journal.remoteSucceeded(operation,task.getIdElement().getIdPart());
        } catch (RuntimeException failure) {
            journal.remoteFailed(operation,failure);
            throw failure;
        }
        try {
            audit.record(new AuditEntry(AuditAction.CREATE,"Task",task.getIdElement().getIdPart(),patientId,
                    AuditOutcome.SUCCESS,true));
            journal.audited(operation);
        } catch (RuntimeException auditFailure) {
            throw new FhirAuditPendingException(operation, auditFailure);
        }
        review.taskCreated(task.getIdElement().getIdPart());
        reviews.saveAndFlush(review);
    }

    @Override @Transactional(readOnly=true)
    public List<EmergencyAccessReview> openReviews(){
        return reviews.findAllByStatusOrderByOccurredAtAsc("OPEN").stream().map(EmergencyAccessReviewEntity::view).toList();
    }

    @Override
    public EmergencyAccessReview review(UUID id,String outcomeCode){
        if(!OUTCOMES.contains(outcomeCode))throw new IllegalArgumentException("Invalid review outcome code");
        EmergencyAccessReviewEntity review=reviews.findById(id).orElseThrow(()->new IllegalArgumentException("Review not found"));
        if(review.view().status().equals("REVIEWED"))throw new IllegalArgumentException("Review is already completed");
        Task task=transport.read(Task.class,review.getTaskId());
        task.setStatus(Task.TaskStatus.COMPLETED);task.setLastModified(new Date());
        task.addOutput().setType(new CodeableConcept().setText("Review outcome"))
                .setValue(new Coding("https://zantrix.org/codes/emergency-review",outcomeCode,outcomeCode));
        UUID operation=journal.begin(FhirOperation.UPDATE,"Task",task.getIdElement().getIdPart(),review.getPatientId());
        try {
            transport.update(task);
            journal.remoteSucceeded(operation,task.getIdElement().getIdPart());
        } catch (RuntimeException failure) {
            journal.remoteFailed(operation,failure);
            throw failure;
        }
        try {
            audit.record(AuditEntry.success(AuditAction.UPDATE,"Task",task.getIdElement().getIdPart(),review.getPatientId()));
            journal.audited(operation);
        } catch (RuntimeException auditFailure) {
            throw new FhirAuditPendingException(operation, auditFailure);
        }
        review.reviewed(SecurityContextHolder.getContext().getAuthentication().getName(),outcomeCode);
        reviews.saveAndFlush(review);
        return review.view();
    }

    private static Task reviewTask(UUID id,String patientId,String resourceType,String resourceId){
        Task task=new Task();task.setId(id.toString());task.setStatus(Task.TaskStatus.READY);task.setIntent(Task.TaskIntent.ORDER);
        task.setPriority(Task.TaskPriority.URGENT);task.setFor(new Reference("Patient/"+patientId));task.setAuthoredOn(new Date());
        task.setCode(new CodeableConcept(new Coding("https://zantrix.org/codes/workflow","emergency-access-review","Emergency access review")));
        task.setDescription("Review emergency access to "+resourceType);
        if(resourceId!=null&&!resourceId.isBlank())task.setFocus(new Reference(resourceType+"/"+resourceId));
        task.setOwner(new Reference().setDisplay("Privacy officers"));return task;
    }
}
