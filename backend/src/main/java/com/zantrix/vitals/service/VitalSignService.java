package com.zantrix.vitals.service;

import com.zantrix.vitals.domain.VitalSignObservationEntity;
import com.zantrix.vitals.repository.VitalSignRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VitalSignService {

    private final VitalSignRepository repository;

    public VitalSignService(VitalSignRepository repository) {
        this.repository = repository;
    }

    public List<VitalSignObservationEntity> getVitalsForPatient(UUID patientId) {
        return repository.findByPatientIdOrderByMeasuredAtDesc(patientId);
    }

    @Transactional
    public List<VitalSignObservationEntity> recordVitals(List<VitalSignObservationEntity> vitals) {
        return repository.saveAll(vitals);
    }
}
