package com.zantrix.adt;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository("coreAdtLocationRepository")
public interface LocationRepository extends JpaRepository<LocationEntity, UUID> {
    List<LocationEntity> findByParentIdAndLevel(UUID parentId, LocationEntity.Level level);
    List<LocationEntity> findByLevel(LocationEntity.Level level);
}
