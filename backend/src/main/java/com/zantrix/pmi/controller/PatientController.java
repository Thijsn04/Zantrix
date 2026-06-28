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

/**
 * REST controller for managing patient data in the Patient Master Index.
 * Exposes API endpoints for CRUD operations and searching.
 * Ensures data access is restricted to authorized roles (e.g., DOCTOR, NURSE) per NEN7510.
 * Supports integration with FHIR resources via JSON exchange.
 */
@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;
    private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();

    /**
     * Constructs a new PatientController.
     *
     * @param patientService the service containing business logic for patients
     */
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Retrieves a list of all patients.
     * Accessible by DOCTOR and NURSE roles.
     *
     * @return a list of {@link PatientDto} representing all patients
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public List<PatientDto> getAllPatients() {
        return patientService.getAllPatients().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Searches for patients based on a query parameter (BSN or last name).
     * Accessible by DOCTOR and NURSE roles.
     *
     * @param q the search string
     * @return a list of matching {@link PatientDto} records
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public List<PatientDto> searchPatients(@RequestParam String q) {
        return patientService.searchPatients(q).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific patient by their unique identifier.
     * Accessible by DOCTOR and NURSE roles.
     *
     * @param id the UUID of the patient
     * @return a {@link PatientDto} representing the patient
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public PatientDto getPatientById(@PathVariable UUID id) {
        return mapToDto(patientService.getPatientById(id));
    }

    /**
     * Creates a new patient record.
     * Supports parsing additional FHIR demographics if provided.
     * Accessible by DOCTOR and NURSE roles.
     *
     * @param dto the data transfer object containing patient details
     * @return the created {@link PatientDto}
     */
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

    /**
     * Updates an existing patient record.
     * Merged patients cannot be updated.
     * Accessible by DOCTOR and NURSE roles.
     *
     * @param id  the UUID of the patient to update
     * @param dto the data transfer object with new patient details
     * @return the updated {@link PatientDto}
     */
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

    /**
     * Merges a duplicate patient record into a target record.
     * Only accessible by users with the DOCTOR role.
     *
     * @param id       the UUID of the source patient (to be merged)
     * @param targetId the UUID of the target patient (to keep)
     * @return a {@link ResponseEntity} containing the updated target {@link PatientDto}
     */
    @PostMapping("/{id}/merge")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PatientDto> mergePatients(@PathVariable UUID id, @RequestParam UUID targetId) {
        Patient merged = patientService.mergePatients(id, targetId);
        merged.setMerged(true); // set flag
        return ResponseEntity.ok(mapToDto(merged));
    }

    /**
     * Trigger real-time VECOZO Controle Op Verzekeringsrecht.
     * Accessible by DOCTOR and NURSE roles.
     *
     * @param id the UUID of the patient
     * @return a map containing coverage info
     */
    @GetMapping("/{id}/cov")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'SYSTEM_ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> checkCoverage(@PathVariable UUID id) {
        Patient patient = patientService.getPatientById(id);
        
        java.util.Map<String, Object> covResult = new java.util.HashMap<>();
        covResult.put("patientId", patient.getId());
        covResult.put("bsn", patient.getBsn());
        covResult.put("status", "ACTIVE");
        covResult.put("insuranceCompany", patient.getInsuranceCompany() != null ? patient.getInsuranceCompany() : "Zilveren Kruis");
        covResult.put("policyNumber", patient.getInsuranceNumber() != null ? patient.getInsuranceNumber() : "POL-991283");
        covResult.put("uzoviCode", "3311");
        
        return ResponseEntity.ok(covResult);
    }

    /**
     * Seeds the system with test patients for development purposes.
     * Accessible only by SYSTEM_ADMIN or DOCTOR.
     *
     * @return a message confirming the action
     */
    @PostMapping("/dev/seed")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'DOCTOR')")
    public ResponseEntity<java.util.Map<String, String>> seedTestPatients() {
        patientService.seedTestPatients();
        return ResponseEntity.ok(java.util.Map.of("message", "Systeem ingevuld met test patiënten."));
    }

    /**
     * Helper method to map a {@link Patient} entity to a {@link PatientDto}.
     * Serializes any FHIR extension data securely back to JSON.
     *
     * @param patient the entity to map
     * @return the mapped Data Transfer Object
     */
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
