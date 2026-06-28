package com.zantrix.cpoe.repository;

import com.zantrix.cpoe.domain.MedicationRequestEntity;
import com.zantrix.cpoe.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MedicationRequestRepository extends JpaRepository<MedicationRequestEntity, UUID> {
    List<MedicationRequestEntity> findByPatientIdAndStatus(UUID patientId, OrderStatus status);
}
