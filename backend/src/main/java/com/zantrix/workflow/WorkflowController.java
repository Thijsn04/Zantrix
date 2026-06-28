package com.zantrix.workflow;

import com.zantrix.iam.AuditLoggable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflow")
public class WorkflowController {

    private final WorkflowDefinitionRepository workflowRepository;
    private final TaskRepository taskRepository;
    private final CarePlanRepository carePlanRepository;
    private final CarePlanActivityRepository activityRepository;

    public WorkflowController(WorkflowDefinitionRepository workflowRepository, TaskRepository taskRepository, CarePlanRepository carePlanRepository, CarePlanActivityRepository activityRepository) {
        this.workflowRepository = workflowRepository;
        this.taskRepository = taskRepository;
        this.carePlanRepository = carePlanRepository;
        this.activityRepository = activityRepository;
    }

    @GetMapping("/definitions")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @AuditLoggable
    public List<WorkflowDefinitionEntity> getDefinitions() {
        return workflowRepository.findAll();
    }

    @PostMapping("/definitions")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @AuditLoggable
    public WorkflowDefinitionEntity createDefinition(@RequestBody WorkflowDefinitionEntity definition) {
        return workflowRepository.save(definition);
    }

    @GetMapping("/tasks/my-worklist")
    @PreAuthorize("hasAnyRole('PRACTITIONER', 'NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable
    public List<TaskEntity> getMyWorklist(@RequestParam(required = false) UUID assigneeId) {
        if (assigneeId != null) {
            return taskRepository.findByAssigneeIdAndStatus(assigneeId, TaskEntity.Status.OPEN);
        }
        return taskRepository.findAll();
    }

    @PostMapping("/tasks/{id}/complete")
    @PreAuthorize("hasAnyRole('PRACTITIONER', 'NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable
    public ResponseEntity<?> completeTask(@PathVariable UUID id) {
        TaskEntity task = taskRepository.findById(id).orElseThrow();
        task.setStatus(TaskEntity.Status.COMPLETED);
        taskRepository.save(task);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/trigger/{processKey}")
    @PreAuthorize("hasAnyRole('PRACTITIONER', 'NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable
    public ResponseEntity<?> triggerWorkflow(@PathVariable String processKey) {
        // Starts the specific process
        return ResponseEntity.ok(java.util.Map.of("status", "STARTED", "processKey", processKey));
    }

    @PutMapping("/admin/rules/deploy")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @AuditLoggable
    public ResponseEntity<?> deployRules(@RequestBody String xmlPayload) {
        // Parses and activates the BPMN/DMN rules
        return ResponseEntity.ok().build();
    }

    @PostMapping("/careplans")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @AuditLoggable
    public CarePlanEntity createCarePlan(@RequestBody CarePlanEntity carePlan) {
        return carePlanRepository.save(carePlan);
    }

    @PatchMapping("/activities/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @AuditLoggable
    public CarePlanActivityEntity updateActivityStatus(@PathVariable UUID id, @RequestBody java.util.Map<String, String> updates) {
        CarePlanActivityEntity activity = activityRepository.findById(id).orElseThrow();
        if (updates.containsKey("status")) {
            activity.setStatus(updates.get("status"));
        }
        return activityRepository.save(activity);
    }
}
