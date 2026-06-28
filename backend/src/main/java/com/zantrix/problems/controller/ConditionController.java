package com.zantrix.problems.controller;

import com.zantrix.problems.domain.ConditionEntity;
import com.zantrix.problems.service.ConditionService;
import com.zantrix.iam.AuditLoggable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/problems")
public class ConditionController {

    private final ConditionService service;

    public ConditionController(ConditionService service) {
        this.service = service;
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ConditionEntity>> getConditions(@PathVariable UUID patientId) {
        return ResponseEntity.ok(service.getConditionsForPatient(patientId));
    }

    @PostMapping
    @AuditLoggable
    public ResponseEntity<ConditionEntity> recordCondition(@RequestBody ConditionEntity condition) {
        return ResponseEntity.ok(service.recordCondition(condition));
    }

    @PutMapping("/{id}/resolve")
    @AuditLoggable
    public ResponseEntity<ConditionEntity> resolveCondition(@PathVariable UUID id, @RequestParam(required = false) LocalDate abatementDate) {
        return ResponseEntity.ok(service.resolveCondition(id, abatementDate));
    }
}
