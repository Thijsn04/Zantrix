package com.zantrix.charting.repository;

import com.zantrix.charting.domain.ClinicalNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ClinicalNoteRepository extends JpaRepository<ClinicalNoteEntity, UUID> {
    List<ClinicalNoteEntity> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    List<ClinicalNoteEntity> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);
}
