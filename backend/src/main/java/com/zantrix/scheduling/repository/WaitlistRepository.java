package com.zantrix.scheduling.repository;

import com.zantrix.scheduling.domain.WaitlistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface WaitlistRepository extends JpaRepository<WaitlistEntity, UUID> {
    List<WaitlistEntity> findByStatus(String status);
}
