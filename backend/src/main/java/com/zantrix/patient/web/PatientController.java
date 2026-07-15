package com.zantrix.patient.web;

import com.zantrix.patient.PatientRegistration;
import com.zantrix.patient.PatientSummary;
import com.zantrix.patient.internal.PatientService;
import com.zantrix.patient.PatientMergeSummary;
import com.zantrix.patient.internal.PatientMergeService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@PreAuthorize("hasAnyRole('PHYSICIAN','NURSE','ADMIN')")
public class PatientController {

    private final PatientService patients;
    private final PatientMergeService merges;

    public PatientController(PatientService patients,PatientMergeService merges) {
        this.patients = patients;
        this.merges = merges;
    }

    @GetMapping
    public List<PatientSummary> search(@RequestParam(required = false) String query,
                                       @RequestParam(required = false) String birthDate,
                                       @RequestParam(required = false) String identifier) {
        return patients.search(query, birthDate, identifier);
    }

    @GetMapping("/{id}")
    public PatientSummary get(@PathVariable String id) {
        return patients.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientSummary register(@Valid @RequestBody PatientRegistration request) {
        return patients.register(request);
    }

    @PostMapping("/duplicates")
    public List<PatientSummary> duplicates(@Valid @RequestBody PatientRegistration request) {
        return patients.findDuplicates(request);
    }

    @PostMapping("/{sourceId}/merge/{targetId}")
    @PreAuthorize("hasAnyRole('PHYSICIAN','ADMIN')")
    public PatientMergeSummary merge(@PathVariable String sourceId,@PathVariable String targetId){
        return merges.merge(sourceId,targetId);
    }

    @PostMapping("/merges/{mergeId}/unmerge")
    @PreAuthorize("hasRole('ADMIN')")
    public PatientMergeSummary unmerge(@PathVariable UUID mergeId){return merges.unmerge(mergeId);}

    @GetMapping("/{patientId}/merges")
    public List<PatientMergeSummary> mergeHistory(@PathVariable String patientId){return merges.history(patientId);}
}
