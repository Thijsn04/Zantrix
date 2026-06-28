package com.zantrix.dwh;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface DwhFactRepository extends JpaRepository<DwhFactEntity, UUID> {
    List<DwhFactEntity> findByMetricName(String metricName);
}
