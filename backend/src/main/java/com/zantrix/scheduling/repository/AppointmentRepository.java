package com.zantrix.scheduling.repository;

import com.zantrix.scheduling.domain.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link AppointmentEntity}.
 * <p>
 * Provides database access operations for managing medical appointments securely.
 * Contains custom queries for conflict detection and schedule retrieval.
 * </p>
 */
@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    
    /**
     * Finds all appointments for a specific practitioner within a given time range.
     *
     * @param practitionerId The UUID of the practitioner.
     * @param start          The start of the time window.
     * @param end            The end of the time window.
     * @return A list of matching {@link AppointmentEntity} objects.
     */
    List<AppointmentEntity> findByPractitionerIdAndStartTimeBetween(UUID practitionerId, OffsetDateTime start, OffsetDateTime end);
    
    /**
     * Finds all scheduled appointments for a specific patient.
     *
     * @param patientId The UUID of the patient.
     * @return A list of the patient's {@link AppointmentEntity} objects.
     */
    List<AppointmentEntity> findByPatientId(UUID patientId);

    /**
     * Detects overlapping appointments for a practitioner within a specific time range.
     * Excludes cancelled appointments from the overlap calculation.
     *
     * @param practitionerId The UUID of the practitioner to check.
     * @param startTime      The proposed start time of the new appointment.
     * @param endTime        The proposed end time of the new appointment.
     * @return A list of any overlapping {@link AppointmentEntity} objects.
     */
    @Query("SELECT a FROM AppointmentEntity a WHERE a.practitionerId = :practitionerId AND a.status != 'CANCELLED' AND a.startTime < :endTime AND a.endTime > :startTime")
    List<AppointmentEntity> findOverlappingAppointments(
            @Param("practitionerId") UUID practitionerId, 
            @Param("startTime") OffsetDateTime startTime, 
            @Param("endTime") OffsetDateTime endTime);
}
