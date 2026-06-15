package com.zantrix.pmi.controller;

import ca.uhn.fhir.context.FhirContext;
import com.zantrix.pmi.domain.Patient;
import com.zantrix.pmi.dto.PatientCreateDto;
import com.zantrix.pmi.dto.PatientDto;
import com.zantrix.pmi.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;
    private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public List<PatientDto> getAllPatients() {
        return patientService.getAllPatients().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public List<PatientDto> searchPatients(@RequestParam String q) {
        return patientService.searchPatients(q).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public PatientDto getPatientById(@PathVariable UUID id) {
        return mapToDto(patientService.getPatientById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public PatientDto createPatient(@RequestBody PatientCreateDto dto) {
        Patient patient = new Patient();
        patient.setBsn(dto.bsn());
        patient.setFirstName(dto.firstName());
        patient.setLastName(dto.lastName());
        patient.setBirthDate(dto.birthDate());
        patient.setGender(dto.gender());
        patient.setEmergency(dto.isEmergency());
        patient.setInsuranceCompany(dto.insuranceCompany());
        patient.setInsuranceNumber(dto.insuranceNumber());
        
        if (dto.fhirDataJson() != null && !dto.fhirDataJson().isEmpty()) {
            patient.setFhirData(FHIR_CONTEXT.newJsonParser().parseResource(org.hl7.fhir.r4.model.Patient.class, dto.fhirDataJson()));
        }

        Patient created = patientService.createPatient(patient);
        return mapToDto(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public PatientDto updatePatient(@PathVariable UUID id, @RequestBody PatientCreateDto dto) {
        Patient patient = new Patient();
        patient.setBsn(dto.bsn());
        patient.setFirstName(dto.firstName());
        patient.setLastName(dto.lastName());
        patient.setBirthDate(dto.birthDate());
        patient.setGender(dto.gender());
        patient.setEmergency(dto.isEmergency());
        patient.setInsuranceCompany(dto.insuranceCompany());
        patient.setInsuranceNumber(dto.insuranceNumber());
        
        if (dto.fhirDataJson() != null && !dto.fhirDataJson().isEmpty()) {
            patient.setFhirData(FHIR_CONTEXT.newJsonParser().parseResource(org.hl7.fhir.r4.model.Patient.class, dto.fhirDataJson()));
        }

        Patient updated = patientService.updatePatient(id, patient);
        return mapToDto(updated);
    }

    @PostMapping("/{id}/merge")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PatientDto> mergePatients(@PathVariable UUID id, @RequestParam UUID targetId) {
        Patient merged = patientService.mergePatients(id, targetId);
        return ResponseEntity.ok(mapToDto(merged));
    }

    private PatientDto mapToDto(Patient patient) {
        String fhirJson = null;
        if (patient.getFhirData() != null) {
            fhirJson = FHIR_CONTEXT.newJsonParser().encodeResourceToString(patient.getFhirData());
        }
        
        return new PatientDto(
            patient.getId(),
            patient.getBsn(),
            patient.getFirstName(),
            patient.getLastName(),
            patient.getBirthDate(),
            patient.getGender(),
            patient.getStatus(),
            patient.isEmergency(),
            patient.getInsuranceCompany(),
            patient.getInsuranceNumber(),
            patient.getMergedIntoId(),
            fhirJson
        );
    }
}
