package com.zantrix.cpoe.repository;

import com.zantrix.cpoe.domain.ServiceRequestEntity;
import com.zantrix.cpoe.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository("cpoeServiceRequestRepository")
public interface ServiceRequestRepository extends JpaRepository<ServiceRequestEntity, UUID> {
    List<ServiceRequestEntity> findByPatientIdAndStatus(UUID patientId, OrderStatus status);
}
