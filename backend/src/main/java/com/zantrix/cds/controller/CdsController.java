package com.zantrix.cds.controller;

import com.zantrix.cds.domain.CdsAlertEntity;
import com.zantrix.cds.service.CdsService;
import com.zantrix.iam.AuditLoggable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cds")
public class CdsController {

    private final CdsService service;

    public CdsController(CdsService service) {
        this.service = service;
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<CdsAlertEntity>> getActiveAlerts(@PathVariable UUID patientId) {
        return ResponseEntity.ok(service.getActiveAlerts(patientId));
    }

    @PutMapping("/alerts/{alertId}/dismiss")
    @AuditLoggable
    public ResponseEntity<Void> dismissAlert(@PathVariable UUID alertId, @RequestParam UUID dismissedBy) {
        service.dismissAlert(alertId, dismissedBy);
        return ResponseEntity.ok().build();
    }
}
