package com.zantrix.problems.repository;

import com.zantrix.problems.domain.ConditionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ConditionRepository extends JpaRepository<ConditionEntity, UUID> {
    List<ConditionEntity> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
