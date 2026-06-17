package com.zantrix.scheduling.service;

import com.zantrix.iam.AuditLoggable;
import com.zantrix.scheduling.domain.AppointmentEntity;
import com.zantrix.scheduling.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service class for managing appointments and resolving scheduling workflows.
 * <p>
 * Contains the business logic to handle practitioner availability, scheduling rules,
 * and double-booking detection. All modifications to the schedule are intercepted
 * and logged to meet MDR/NEN7510 audit requirements.
 * </p>
 */
@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository repository;

    /**
     * Constructs a new {@link AppointmentService}.
     *
     * @param repository The repository for persisting appointment data.
     */
    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves all appointments for a specific practitioner within a given time range.
     *
     * @param practitionerId The UUID of the practitioner.
     * @param start          The start time of the query window.
     * @param end            The end time of the query window.
     * @return A list of {@link AppointmentEntity} objects.
     */
    public List<AppointmentEntity> getAppointmentsForPractitioner(UUID practitionerId, OffsetDateTime start, OffsetDateTime end) {
        return repository.findByPractitionerIdAndStartTimeBetween(practitionerId, start, end);
    }

    /**
     * Retrieves all scheduled appointments for a specific patient.
     *
     * @param patientId The UUID of the patient.
     * @return A list of {@link AppointmentEntity} objects for the patient.
     */
    public List<AppointmentEntity> getAppointmentsForPatient(UUID patientId) {
        return repository.findByPatientId(patientId);
    }

    /**
     * Checks for any overlapping appointments for a practitioner within the specified time range.
     *
     * @param practitionerId The UUID of the practitioner.
     * @param start          The proposed start time.
     * @param end            The proposed end time.
     * @return A list of overlapping {@link AppointmentEntity} objects.
     */
    public List<AppointmentEntity> checkOverlaps(UUID practitionerId, OffsetDateTime start, OffsetDateTime end) {
        return repository.findOverlappingAppointments(practitionerId, start, end);
    }

    /**
     * Schedules a new medical appointment.
     *
     * @param appointment The {@link AppointmentEntity} containing the scheduling details.
     * @return The persisted {@link AppointmentEntity}.
     */
    @AuditLoggable
    public AppointmentEntity createAppointment(AppointmentEntity appointment) {
        List<AppointmentEntity> overlaps = checkOverlaps(
                appointment.getPractitionerId(),
                appointment.getStartTime(),
                appointment.getEndTime());
        if (!overlaps.isEmpty()) {
            throw new IllegalArgumentException("Practitioner is double-booked during this time");
        }
        return repository.save(appointment);
    }

    /**
     * Updates an existing medical appointment (e.g., rescheduling or status change).
     *
     * @param id      The UUID of the appointment to update.
     * @param updated The new details for the appointment.
     * @return The updated {@link AppointmentEntity}.
     */
    @AuditLoggable
    public AppointmentEntity updateAppointment(UUID id, AppointmentEntity updated) {
        AppointmentEntity existing = repository.findById(id).orElseThrow();
        
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setStatus(updated.getStatus());
        existing.setPractitionerId(updated.getPractitionerId());
        existing.setLocationId(updated.getLocationId());
        existing.setFhirData(updated.getFhirData());
        
        return repository.save(existing);
    }
}
