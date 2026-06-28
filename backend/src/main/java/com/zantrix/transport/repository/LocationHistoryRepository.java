package com.zantrix.transport.repository;

import com.zantrix.transport.domain.LocationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface LocationHistoryRepository extends JpaRepository<LocationHistoryEntity, UUID> {
    List<LocationHistoryEntity> findByPatientId(UUID patientId);
}
