package com.zantrix.pmi.service;

import com.zantrix.iam.AuditLoggable;
import com.zantrix.pmi.domain.Patient;
import com.zantrix.pmi.domain.PatientStatus;
import com.zantrix.pmi.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service class for managing patient records in the Patient Master Index.
 * Encapsulates business logic, BSN validation, and ensures compliance with MDR and NEN7510.
 * Methods modifying or retrieving sensitive data trigger audit logs.
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;

    /**
     * Constructs a new PatientService with the required repository.
     *
     * @param patientRepository the repository for patient data operations
     */
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    /**
     * Retrieves all patients in the system.
     *
     * @return a list of all {@link Patient} records
     */
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    /**
     * Searches for patients using a specific query.
     * Searches against BSN or last name.
     *
     * @param query the search string
     * @return a list of matching {@link Patient} records
     */
    public List<Patient> searchPatients(String query) {
        if (query == null || query.isBlank()) {
            return patientRepository.findAll();
        }
        return patientRepository.searchPatients(query);
    }

    /**
     * Retrieves a patient by their unique identifier.
     * This read operation is audit logged per NEN7510 requirements.
     *
     * @param id the UUID of the patient
     * @return the {@link Patient} record
     * @throws RuntimeException if the patient is not found
     */
    @AuditLoggable(patientIdParam = "id")
    public Patient getPatientById(UUID id) {
        return patientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    /**
     * Creates a new patient record.
     * Performs BSN validation unless it is an emergency registration.
     *
     * @param patient the patient entity to be created
     * @return the saved {@link Patient} record
     * @throws IllegalArgumentException if the BSN is invalid or already exists
     */
    @Transactional
    public Patient createPatient(Patient patient) {
        if (!patient.isEmergency()) {
            validateBsn(patient.getBsn());
            if (patientRepository.findByBsn(patient.getBsn()).isPresent()) {
                throw new IllegalArgumentException("Patient with this BSN already exists");
            }
        }
        return patientRepository.save(patient);
    }

    /**
     * Updates an existing patient record.
     * Merged patients cannot be updated. This operation is audit logged.
     *
     * @param id             the UUID of the patient to update
     * @param updatedDetails the new details for the patient
     * @return the updated {@link Patient} record
     * @throws IllegalArgumentException if the patient is merged or the BSN is invalid
     */
    @Transactional
    @AuditLoggable(patientIdParam = "id")
    public Patient updatePatient(UUID id, Patient updatedDetails) {
        Patient existing = getPatientById(id);
        if (existing.getStatus() == PatientStatus.MERGED) {
            throw new IllegalArgumentException("Cannot update a merged patient");
        }
        
        existing.setFirstName(updatedDetails.getFirstName());
        existing.setLastName(updatedDetails.getLastName());
        existing.setBirthDate(updatedDetails.getBirthDate());
        existing.setGender(updatedDetails.getGender());
        existing.setInsuranceCompany(updatedDetails.getInsuranceCompany());
        existing.setInsuranceNumber(updatedDetails.getInsuranceNumber());
        existing.setFhirData(updatedDetails.getFhirData());
        
        if (!existing.isEmergency() && updatedDetails.getBsn() != null) {
            validateBsn(updatedDetails.getBsn());
            existing.setBsn(updatedDetails.getBsn());
        }

        return patientRepository.save(existing);
    }

    /**
     * Merges a source patient record into a target patient record.
     * Marks the source patient as MERGED.
     *
     * @param sourceId the UUID of the patient to be merged and deactivated
     * @param targetId the UUID of the patient that will remain active
     * @return the target {@link Patient} record
     * @throws IllegalArgumentException if either patient is already merged
     */
    @Transactional
    public Patient mergePatients(UUID sourceId, UUID targetId) {
        Patient source = getPatientById(sourceId);
        Patient target = getPatientById(targetId);

        if (source.getStatus() == PatientStatus.MERGED || target.getStatus() == PatientStatus.MERGED) {
            throw new IllegalArgumentException("One of the patients is already merged");
        }

        source.setStatus(PatientStatus.MERGED);
        source.setMergedIntoId(targetId);
        patientRepository.save(source);
        
        return target;
    }

    /**
     * Validates a Dutch Citizen Service Number (BSN) using the 11-test algorithm.
     * Required for non-emergency patient registrations.
     *
     * @param bsn the BSN to validate
     * @throws IllegalArgumentException if the BSN is null, empty, or fails the 11-test
     */
    public void validateBsn(String bsn) {
        if (bsn == null || bsn.trim().isEmpty()) {
            throw new IllegalArgumentException("BSN cannot be null or empty for non-emergency patients");
        }
        
        String cleanBsn = bsn.replaceAll("[^0-9]", "");
        if (cleanBsn.length() == 8) {
            cleanBsn = "0" + cleanBsn; // Pad with 0 for 8-digit BSN
        }
        if (cleanBsn.length() != 9) {
            throw new IllegalArgumentException("Invalid BSN length");
        }

        int sum = 0;
        for (int i = 0; i < 8; i++) {
            sum += Character.getNumericValue(cleanBsn.charAt(i)) * (9 - i);
        }
        sum += Character.getNumericValue(cleanBsn.charAt(8)) * -1;

        if (sum % 11 != 0) {
            throw new IllegalArgumentException("Invalid BSN according to 11-proef");
        }
    }
}
