package com.zantrix.terminology.repository;

import com.zantrix.terminology.domain.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link ImportJob} entity.
 * <p>
 * Manages the persistence of terminology import state. This prevents
 * redundant uploading of large SNOMED/LOINC datasets during application restarts.
 * </p>
 */
public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {
    
    /**
     * Finds an import job by the name of the imported file.
     *
     * @param fileName The name of the file to search for.
     * @return An {@link Optional} containing the {@link ImportJob} if it exists.
     */
    Optional<ImportJob> findByFileName(String fileName);
}
