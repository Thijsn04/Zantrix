package com.zantrix.medications.web;

import com.zantrix.cds.SafetyAssessment;
import com.zantrix.medications.PrescriptionRequest;
import com.zantrix.medications.PrescriptionSummary;
import com.zantrix.medications.internal.MedicationService;
import com.zantrix.medications.MedicationEventRequest;
import com.zantrix.medications.MedicationEventSummary;
import com.zantrix.medications.MedicationReconciliationRequest;
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
@RequestMapping("/api/v1/medications")
@PreAuthorize("hasAnyRole('PHYSICIAN','NURSE','PHARMACIST')")
public class MedicationController {
    private final MedicationService medications;
    public MedicationController(MedicationService medications) { this.medications = medications; }

    @GetMapping("/safety-check")
    public SafetyAssessment check(@RequestParam String patientId, @RequestParam String rxNormIngredientCode,
                                  @RequestParam String display) {
        return medications.check(patientId, rxNormIngredientCode, display);
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PHYSICIAN','PHARMACIST')")
    public PrescriptionSummary prescribe(@Valid @RequestBody PrescriptionRequest request) {
        return medications.prescribe(request);
    }

    @GetMapping
    public List<PrescriptionSummary> list(@RequestParam String patientId,
                                          @RequestParam(defaultValue = "false") boolean includeStopped) {
        return medications.list(patientId, includeStopped);
    }

    @PostMapping("/{id}/stop")
    public PrescriptionSummary stop(@PathVariable String id, @RequestParam String patientId,
                                    @RequestParam String reason) {
        return medications.stop(id, patientId, reason);
    }

    @PostMapping("/reconciliation") @ResponseStatus(HttpStatus.CREATED)
    public MedicationEventSummary reconcile(@Valid @RequestBody MedicationReconciliationRequest request) {
        return medications.reconcile(request);
    }

    @PostMapping("/administrations") @ResponseStatus(HttpStatus.CREATED)
    public MedicationEventSummary administer(@Valid @RequestBody MedicationEventRequest request) {
        return medications.administer(request);
    }

    @PostMapping("/dispenses") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PHARMACIST')")
    public MedicationEventSummary dispense(@Valid @RequestBody MedicationEventRequest request) {
        return medications.dispense(request);
    }
}
