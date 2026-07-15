package com.zantrix.platform.fhir.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

interface EmergencyAccessReviewRepository extends JpaRepository<EmergencyAccessReviewEntity, UUID> {
    List<EmergencyAccessReviewEntity> findAllByStatusOrderByOccurredAtAsc(String status);
}
