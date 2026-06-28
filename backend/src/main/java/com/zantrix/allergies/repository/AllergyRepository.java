package com.zantrix.allergies.repository;

import com.zantrix.allergies.domain.AllergyIntoleranceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AllergyRepository extends JpaRepository<AllergyIntoleranceEntity, UUID> {
    List<AllergyIntoleranceEntity> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
