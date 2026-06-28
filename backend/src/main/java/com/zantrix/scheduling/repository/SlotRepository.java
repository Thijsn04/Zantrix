package com.zantrix.scheduling.repository;

import com.zantrix.scheduling.domain.SlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface SlotRepository extends JpaRepository<SlotEntity, UUID> {
    List<SlotEntity> findByPractitionerIdAndStatus(UUID practitionerId, String status);
}
