package com.zantrix.charting.repository;

import com.zantrix.charting.domain.ClinicalNoteAddendumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ClinicalNoteAddendumRepository extends JpaRepository<ClinicalNoteAddendumEntity, UUID> {
    List<ClinicalNoteAddendumEntity> findByOriginalNoteIdOrderByAddedAtAsc(UUID originalNoteId);
}
