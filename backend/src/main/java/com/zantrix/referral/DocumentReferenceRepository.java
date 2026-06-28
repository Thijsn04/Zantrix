package com.zantrix.referral;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface DocumentReferenceRepository extends JpaRepository<DocumentReferenceEntity, UUID> {
    List<DocumentReferenceEntity> findByServiceRequestId(UUID serviceRequestId);
}
