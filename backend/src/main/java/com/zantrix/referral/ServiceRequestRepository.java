package com.zantrix.referral;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository("referralServiceRequestRepository")
public interface ServiceRequestRepository extends JpaRepository<ServiceRequestEntity, UUID> {
    List<ServiceRequestEntity> findByStatus(ServiceRequestEntity.Status status);
}
