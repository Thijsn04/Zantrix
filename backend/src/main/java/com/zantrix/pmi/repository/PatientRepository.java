package com.zantrix.pmi.repository;

import com.zantrix.pmi.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the {@link Patient} entity.
 * Provides database access operations for managing patient records securely.
 * Access to this repository should be controlled to adhere to NEN7510.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    
    /**
     * Finds a patient by their unique Citizen Service Number (BSN).
     *
     * @param bsn the Citizen Service Number
     * @return an {@link Optional} containing the patient if found
     */
    Optional<Patient> findByBsn(String bsn);
    
    /**
     * Finds patients whose last name contains the given string, ignoring case.
     *
     * @param lastName the partial last name to search for
     * @return a list of matching patients
     */
    List<Patient> findByLastNameContainingIgnoreCase(String lastName);
    
    /**
     * Searches for patients by exact BSN or partial, case-insensitive last name.
     *
     * @param query the search term (BSN or part of the last name)
     * @return a list of matching patients
     */
    @Query("SELECT p FROM Patient p WHERE p.bsn = :query OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Patient> searchPatients(@Param("query") String query);
}
