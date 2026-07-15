package com.zantrix.patient.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

interface PatientMergeRepository extends JpaRepository<PatientMergeEntity, UUID> {
    List<PatientMergeEntity> findAllBySourceIdOrTargetIdOrderByPreparedAtDesc(String sourceId,String targetId);
}
