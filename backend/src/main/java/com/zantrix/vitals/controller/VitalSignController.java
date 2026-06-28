package com.zantrix.vitals.controller;

import com.zantrix.vitals.domain.VitalSignObservationEntity;
import com.zantrix.vitals.service.VitalSignService;
import com.zantrix.iam.AuditLoggable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vitals")
public class VitalSignController {

    private final VitalSignService service;

    public VitalSignController(VitalSignService service) {
        this.service = service;
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<VitalSignObservationEntity>> getVitals(@PathVariable UUID patientId) {
        return ResponseEntity.ok(service.getVitalsForPatient(patientId));
    }

    @PostMapping("/batch")
    @AuditLoggable
    public ResponseEntity<List<VitalSignObservationEntity>> recordVitals(@RequestBody List<VitalSignObservationEntity> vitals) {
        return ResponseEntity.ok(service.recordVitals(vitals));
    }
}
