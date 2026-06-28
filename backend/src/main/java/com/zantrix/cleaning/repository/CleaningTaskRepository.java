package com.zantrix.cleaning.repository;

import com.zantrix.cleaning.domain.CleaningTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface CleaningTaskRepository extends JpaRepository<CleaningTaskEntity, UUID> {
    List<CleaningTaskEntity> findByStatus(String status);
}
