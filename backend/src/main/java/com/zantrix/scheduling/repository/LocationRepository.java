package com.zantrix.scheduling.repository;

import com.zantrix.scheduling.domain.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository("schedulingLocationRepository")
public interface LocationRepository extends JpaRepository<LocationEntity, UUID> {
    List<LocationEntity> findByTypeAndStatus(String type, String status);
}
