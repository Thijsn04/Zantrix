package com.zantrix.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface CarePlanActivityRepository extends JpaRepository<CarePlanActivityEntity, UUID> {
    List<CarePlanActivityEntity> findByCarePlanId(UUID carePlanId);
}
