package com.zantrix.allergies.controller;

import com.zantrix.allergies.domain.AllergyIntoleranceEntity;
import com.zantrix.allergies.service.AllergyService;
import com.zantrix.iam.AuditLoggable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/allergies")
public class AllergyController {

    private final AllergyService service;

    public AllergyController(AllergyService service) {
        this.service = service;
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AllergyIntoleranceEntity>> getAllergies(@PathVariable UUID patientId) {
        return ResponseEntity.ok(service.getAllergiesForPatient(patientId));
    }

    @PostMapping
    @AuditLoggable
    public ResponseEntity<AllergyIntoleranceEntity> recordAllergy(@RequestBody AllergyIntoleranceEntity allergy) {
        return ResponseEntity.ok(service.recordAllergy(allergy));
    }
}
