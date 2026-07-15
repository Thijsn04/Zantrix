package com.zantrix.vitals.web;

import com.zantrix.vitals.VitalSummary;
import com.zantrix.vitals.VitalsRequest;
import com.zantrix.vitals.internal.VitalsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vitals")
@PreAuthorize("hasAnyRole('PHYSICIAN','NURSE')")
public class VitalsController {
    private final VitalsService vitals;
    public VitalsController(VitalsService vitals) { this.vitals = vitals; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public List<VitalSummary> record(@Valid @RequestBody VitalsRequest request) { return vitals.record(request); }

    @GetMapping
    public List<VitalSummary> list(@RequestParam String patientId,
                                   @RequestParam(defaultValue = "100") int count) {
        return vitals.list(patientId, count);
    }
}
