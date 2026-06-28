package com.zantrix.problems.service;

import com.zantrix.problems.domain.ConditionEntity;
import com.zantrix.problems.domain.ClinicalStatus;
import com.zantrix.problems.repository.ConditionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ConditionService {

    private final ConditionRepository repository;

    public ConditionService(ConditionRepository repository) {
        this.repository = repository;
    }

    public List<ConditionEntity> getConditionsForPatient(UUID patientId) {
        return repository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Transactional
    public ConditionEntity recordCondition(ConditionEntity condition) {
        return repository.save(condition);
    }

    @Transactional
    public ConditionEntity resolveCondition(UUID conditionId, LocalDate abatementDate) {
        ConditionEntity condition = repository.findById(conditionId)
            .orElseThrow(() -> new IllegalArgumentException("Condition not found"));
        
        condition.setClinicalStatus(ClinicalStatus.RESOLVED);
        condition.setAbatementDate(abatementDate != null ? abatementDate : LocalDate.now());
        return repository.save(condition);
    }
}
