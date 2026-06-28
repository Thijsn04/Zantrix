package com.zantrix.logistics;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface TransportJobRepository extends JpaRepository<TransportJobEntity, UUID> {
    List<TransportJobEntity> findByStatus(TransportJobEntity.Status status);
}
