package com.zantrix.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface CarePlanRepository extends JpaRepository<CarePlanEntity, UUID> {
    List<CarePlanEntity> findByPatientId(UUID patientId);
}
