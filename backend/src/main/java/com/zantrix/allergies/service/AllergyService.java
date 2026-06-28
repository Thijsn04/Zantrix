package com.zantrix.allergies.service;

import com.zantrix.allergies.domain.AllergyIntoleranceEntity;
import com.zantrix.allergies.repository.AllergyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AllergyService {

    private final AllergyRepository repository;

    public AllergyService(AllergyRepository repository) {
        this.repository = repository;
    }

    public List<AllergyIntoleranceEntity> getAllergiesForPatient(UUID patientId) {
        return repository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Transactional
    public AllergyIntoleranceEntity recordAllergy(AllergyIntoleranceEntity allergy) {
        return repository.save(allergy);
    }
}
