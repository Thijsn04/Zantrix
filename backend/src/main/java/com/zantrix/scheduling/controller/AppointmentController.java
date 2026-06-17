package com.zantrix.scheduling.controller;

import com.zantrix.scheduling.domain.AppointmentEntity;
import com.zantrix.scheduling.service.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing medical appointments.
 * <p>
 * Provides endpoints for retrieving, creating, and updating appointments. This acts as the
 * primary interface for the Scheduling module, supporting integrations with the frontend
 * and ensuring that appointment operations follow standard business rules.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService service;

    /**
     * Constructs a new {@link AppointmentController}.
     *
     * @param service The service handling appointment business logic.
     */
    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    /**
     * Retrieves a list of appointments based on the provided search criteria.
     * Can fetch appointments for a specific practitioner within a time range or for a specific patient.
     *
     * @param practitionerId The UUID of the practitioner (optional).
     * @param patientId      The UUID of the patient (optional).
     * @param start          The start time of the search window (optional).
     * @param end            The end time of the search window (optional).
     * @return A list of {@link AppointmentEntity} objects matching the criteria.
     */
    @GetMapping
    public List<AppointmentEntity> getAppointments(
            @RequestParam(required = false) UUID practitionerId,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end) {
        
        if (practitionerId != null && start != null && end != null) {
            return service.getAppointmentsForPractitioner(practitionerId, start, end);
        } else if (patientId != null) {
            return service.getAppointmentsForPatient(patientId);
        }
        // In a real scenario we might return a bad request if no valid combination is provided.
        return List.of();
    }

    /**
     * Creates a new appointment record.
     *
     * @param appointment The {@link AppointmentEntity} containing the appointment details.
     * @return A {@link ResponseEntity} containing the created appointment.
     */
    @PostMapping
    public ResponseEntity<AppointmentEntity> createAppointment(@RequestBody AppointmentEntity appointment) {
        return ResponseEntity.ok(service.createAppointment(appointment));
    }

    /**
     * Updates an existing appointment record.
     *
     * @param id          The UUID of the appointment to update.
     * @param appointment The updated {@link AppointmentEntity} details.
     * @return A {@link ResponseEntity} containing the updated appointment.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentEntity> updateAppointment(@PathVariable UUID id, @RequestBody AppointmentEntity appointment) {
        return ResponseEntity.ok(service.updateAppointment(id, appointment));
    }
}
