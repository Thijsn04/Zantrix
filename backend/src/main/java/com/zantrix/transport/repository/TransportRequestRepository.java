package com.zantrix.transport.repository;

import com.zantrix.transport.domain.TransportRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface TransportRequestRepository extends JpaRepository<TransportRequestEntity, UUID> {
    List<TransportRequestEntity> findByStatus(String status);
}
