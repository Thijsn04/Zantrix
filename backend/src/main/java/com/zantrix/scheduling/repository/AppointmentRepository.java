package com.zantrix.scheduling.repository;

import com.zantrix.scheduling.domain.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    
    List<AppointmentEntity> findByPractitionerIdAndStartTimeBetween(UUID practitionerId, OffsetDateTime start, OffsetDateTime end);
    
    List<AppointmentEntity> findByPatientId(UUID patientId);

    @Query("SELECT a FROM AppointmentEntity a WHERE a.practitionerId = :practitionerId AND a.status != 'CANCELLED' AND a.startTime < :endTime AND a.endTime > :startTime")
    List<AppointmentEntity> findOverlappingAppointments(
            @Param("practitionerId") UUID practitionerId, 
            @Param("startTime") OffsetDateTime startTime, 
            @Param("endTime") OffsetDateTime endTime);
}
