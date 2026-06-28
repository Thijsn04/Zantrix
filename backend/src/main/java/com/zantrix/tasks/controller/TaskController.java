package com.zantrix.tasks.controller;

import com.zantrix.tasks.domain.TaskEntity;
import com.zantrix.tasks.service.TaskService;
import com.zantrix.iam.AuditLoggable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<TaskEntity>> getPatientTasks(@PathVariable UUID patientId) {
        return ResponseEntity.ok(service.getTasksForPatient(patientId));
    }

    @GetMapping("/user/{assigneeId}")
    public ResponseEntity<List<TaskEntity>> getUserTasks(@PathVariable UUID assigneeId) {
        return ResponseEntity.ok(service.getTasksForUser(assigneeId));
    }

    @PostMapping
    @AuditLoggable
    public ResponseEntity<TaskEntity> createTask(@RequestBody TaskEntity task) {
        return ResponseEntity.ok(service.createTask(task));
    }

    @PutMapping("/{id}/complete")
    @AuditLoggable
    public ResponseEntity<TaskEntity> completeTask(@PathVariable UUID id) {
        return ResponseEntity.ok(service.completeTask(id));
    }
}
