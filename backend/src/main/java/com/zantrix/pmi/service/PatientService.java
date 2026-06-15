package com.zantrix.pmi.service;

import com.zantrix.iam.AuditLoggable;
import com.zantrix.pmi.domain.Patient;
import com.zantrix.pmi.domain.PatientStatus;
import com.zantrix.pmi.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public List<Patient> searchPatients(String query) {
        if (query == null || query.isBlank()) {
            return patientRepository.findAll();
        }
        return patientRepository.searchPatients(query);
    }

    @AuditLoggable(patientIdParam = "id")
    public Patient getPatientById(UUID id) {
        return patientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    @Transactional
    public Patient createPatient(Patient patient) {
        if (!patient.isEmergency()) {
            validateBsn(patient.getBsn());
            if (patientRepository.findByBsn(patient.getBsn()).isPresent()) {
                throw new RuntimeException("Patient with this BSN already exists");
            }
        }
        return patientRepository.save(patient);
    }

    @Transactional
    @AuditLoggable(patientIdParam = "id")
    public Patient updatePatient(UUID id, Patient updatedDetails) {
        Patient existing = getPatientById(id);
        if (existing.getStatus() == PatientStatus.MERGED) {
            throw new RuntimeException("Cannot update a merged patient");
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

    @Transactional
    public Patient mergePatients(UUID sourceId, UUID targetId) {
        Patient source = getPatientById(sourceId);
        Patient target = getPatientById(targetId);

        if (source.getStatus() == PatientStatus.MERGED || target.getStatus() == PatientStatus.MERGED) {
            throw new RuntimeException("One of the patients is already merged");
        }

        source.setStatus(PatientStatus.MERGED);
        source.setMergedIntoId(targetId);
        patientRepository.save(source);
        
        return target;
    }

    public void validateBsn(String bsn) {
        if (bsn == null || bsn.trim().isEmpty()) {
            throw new RuntimeException("BSN cannot be null or empty for non-emergency patients");
        }
        
        String cleanBsn = bsn.replaceAll("[^0-9]", "");
        if (cleanBsn.length() == 8) {
            cleanBsn = "0" + cleanBsn; // Pad with 0 for 8-digit BSN
        }
        if (cleanBsn.length() != 9) {
            throw new RuntimeException("Invalid BSN length");
        }

        int sum = 0;
        for (int i = 0; i < 8; i++) {
            sum += Character.getNumericValue(cleanBsn.charAt(i)) * (9 - i);
        }
        sum += Character.getNumericValue(cleanBsn.charAt(8)) * -1;

        if (sum % 11 != 0) {
            throw new RuntimeException("Invalid BSN according to 11-proef");
        }
    }
}
