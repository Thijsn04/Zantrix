package com.zantrix.workflow.web;

import com.zantrix.workflow.TaskRequest;
import com.zantrix.workflow.TaskSummary;
import com.zantrix.workflow.internal.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@PreAuthorize("hasAnyRole('PHYSICIAN','NURSE','PHARMACIST','ADMIN','PRIVACY_OFFICER')")
public class WorkflowController {
    private final WorkflowService workflow;
    public WorkflowController(WorkflowService workflow) { this.workflow = workflow; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public TaskSummary create(@Valid @RequestBody TaskRequest request) { return workflow.create(request); }

    @GetMapping
    public List<TaskSummary> list(@RequestParam(required = false) String owner,
                                  @RequestParam(required = false) String patientId,
                                  @RequestParam(defaultValue = "false") boolean includeCompleted) {
        return workflow.worklist(owner, patientId, includeCompleted);
    }

    @PostMapping("/{id}/claim")
    public TaskSummary claim(@PathVariable String id, @RequestParam String patientId,
                             @RequestParam String ownerReference) { return workflow.claim(id, patientId, ownerReference); }

    @PostMapping("/{id}/start")
    public TaskSummary start(@PathVariable String id, @RequestParam String patientId) {
        return workflow.start(id, patientId);
    }

    @PostMapping("/{id}/complete")
    public TaskSummary complete(@PathVariable String id, @RequestParam String patientId,
                                @RequestParam(required = false) String outcome) {
        return workflow.complete(id, patientId, outcome);
    }
}
