package com.zantrix.vitals.repository;

import com.zantrix.vitals.domain.VitalSignObservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface VitalSignRepository extends JpaRepository<VitalSignObservationEntity, UUID> {
    List<VitalSignObservationEntity> findByPatientIdOrderByMeasuredAtDesc(UUID patientId);
}
