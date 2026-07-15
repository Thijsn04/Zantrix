package com.zantrix.workflow.internal;

import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import com.zantrix.workflow.TaskRequest;
import com.zantrix.workflow.TaskSummary;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class WorkflowService {
    private final FhirAccessGateway fhir;
    public WorkflowService(FhirAccessGateway fhir) { this.fhir = fhir; }

    public TaskSummary create(TaskRequest request) {
        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setStatus(Task.TaskStatus.READY);
        task.setIntent(Task.TaskIntent.ORDER);
        task.setPriority(request.priority() == null ? Task.TaskPriority.ROUTINE
                : Task.TaskPriority.fromCode(request.priority().toLowerCase(Locale.ROOT)));
        task.setFor(new Reference("Patient/" + request.patientId()));
        if (request.encounterId() != null && !request.encounterId().isBlank()) {
            task.setEncounter(new Reference("Encounter/" + request.encounterId()));
        }
        if (request.focusReference() != null && !request.focusReference().isBlank()) {
            task.setFocus(new Reference(request.focusReference()));
        }
        if (request.ownerReference() != null && !request.ownerReference().isBlank()) {
            task.setOwner(new Reference(request.ownerReference()));
        }
        task.setDescription(request.description());
        task.setAuthoredOn(new Date());
        if (request.dueAt() != null) {
            task.getRestriction().setPeriod(new Period().setEnd(Date.from(request.dueAt())));
        }
        fhir.create(task, request.patientId());
        return summary(task);
    }

    public List<TaskSummary> worklist(String owner, String patientId, boolean includeCompleted) {
        Map<String, List<String>> parameters = new LinkedHashMap<>();
        parameters.put("_sort", List.of("-authored-on"));
        parameters.put("_count", List.of("300"));
        if (owner != null && !owner.isBlank()) parameters.put("owner", List.of(owner));
        if (patientId != null && !patientId.isBlank()) parameters.put("patient", List.of(patientId));
        if (!includeCompleted) parameters.put("status", List.of("requested,received,accepted,ready,in-progress,on-hold"));
        return FhirBundles.resources(fhir.search("Task", parameters, patientId), Task.class)
                .stream().map(WorkflowService::summary).toList();
    }

    public TaskSummary claim(String id, String patientId, String ownerReference) {
        Task task = fhir.read(Task.class, id, patientId);
        requireStatus(task, Task.TaskStatus.REQUESTED, Task.TaskStatus.RECEIVED, Task.TaskStatus.READY);
        task.setOwner(new Reference(ownerReference));
        task.setStatus(Task.TaskStatus.ACCEPTED);
        task.setLastModified(new Date());
        fhir.update(task, patientId);
        return summary(task);
    }

    public TaskSummary start(String id, String patientId) {
        Task task = fhir.read(Task.class, id, patientId);
        requireStatus(task, Task.TaskStatus.ACCEPTED, Task.TaskStatus.READY);
        task.setStatus(Task.TaskStatus.INPROGRESS);
        task.setLastModified(new Date());
        fhir.update(task, patientId);
        return summary(task);
    }

    public TaskSummary complete(String id, String patientId, String outcome) {
        Task task = fhir.read(Task.class, id, patientId);
        requireStatus(task, Task.TaskStatus.INPROGRESS, Task.TaskStatus.ACCEPTED, Task.TaskStatus.READY);
        task.setStatus(Task.TaskStatus.COMPLETED);
        task.setLastModified(new Date());
        if (outcome != null && !outcome.isBlank()) {
            task.addOutput().setType(new CodeableConcept().setText("Outcome"))
                    .setValue(new org.hl7.fhir.r4.model.StringType(outcome));
        }
        fhir.update(task, patientId);
        return summary(task);
    }

    private static void requireStatus(Task task, Task.TaskStatus... statuses) {
        for (Task.TaskStatus status : statuses) if (task.getStatus() == status) return;
        throw new IllegalArgumentException("Task transition is not valid from status " + task.getStatus().toCode());
    }

    private static TaskSummary summary(Task task) {
        return new TaskSummary(task.getIdElement().getIdPart(),
                task.getFor().getReferenceElement().getIdPart(), task.getStatus().toCode(),
                task.getPriority().toCode(), task.getDescription(), task.hasFocus() ? task.getFocus().getReference() : null,
                task.hasOwner() ? task.getOwner().getReference() : null,
                task.getAuthoredOn() == null ? null : task.getAuthoredOn().toInstant(),
                task.getRestriction().getPeriod().getEnd() == null ? null
                        : task.getRestriction().getPeriod().getEnd().toInstant());
    }
}
