package com.zantrix.pmi.repository;

import com.zantrix.pmi.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    Optional<Patient> findByBsn(String bsn);
    List<Patient> findByLastNameContainingIgnoreCase(String lastName);
    
    @Query("SELECT p FROM Patient p WHERE p.bsn = :query OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Patient> searchPatients(@Param("query") String query);
}
