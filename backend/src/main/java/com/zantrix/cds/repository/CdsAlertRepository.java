package com.zantrix.cds.repository;

import com.zantrix.cds.domain.CdsAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CdsAlertRepository extends JpaRepository<CdsAlertEntity, UUID> {
    List<CdsAlertEntity> findByPatientIdAndDismissedFalseOrderByCreatedAtDesc(UUID patientId);
}
