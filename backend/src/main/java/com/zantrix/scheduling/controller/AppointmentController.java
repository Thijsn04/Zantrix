package com.zantrix.scheduling.controller;

import com.zantrix.scheduling.domain.AppointmentEntity;
import com.zantrix.scheduling.service.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

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

    @PostMapping
    public ResponseEntity<AppointmentEntity> createAppointment(@RequestBody AppointmentEntity appointment) {
        return ResponseEntity.ok(service.createAppointment(appointment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentEntity> updateAppointment(@PathVariable UUID id, @RequestBody AppointmentEntity appointment) {
        return ResponseEntity.ok(service.updateAppointment(id, appointment));
    }
}
