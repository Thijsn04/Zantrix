package com.zantrix.scheduling.web;

import com.zantrix.scheduling.AppointmentRequest;
import com.zantrix.scheduling.AppointmentSummary;
import com.zantrix.scheduling.internal.SchedulingService;
import com.zantrix.scheduling.ScheduleRequest;
import com.zantrix.scheduling.SlotSummary;
import jakarta.validation.Valid;
import org.hl7.fhir.r4.model.Appointment;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/appointments")
@PreAuthorize("hasAnyRole('PHYSICIAN','NURSE','ADMIN')")
public class SchedulingController {

    private final SchedulingService scheduling;

    public SchedulingController(SchedulingService scheduling) {
        this.scheduling = scheduling;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentSummary book(@Valid @RequestBody AppointmentRequest request) {
        return scheduling.book(request);
    }

    @PostMapping("/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public List<SlotSummary> schedule(@Valid @RequestBody ScheduleRequest request) {
        return scheduling.createSchedule(request);
    }

    @GetMapping("/availability")
    public List<SlotSummary> availability(@RequestParam String practitionerId,
                                          @RequestParam Instant start, @RequestParam Instant end) {
        return scheduling.availability(practitionerId, start, end);
    }

    @GetMapping
    public List<AppointmentSummary> list(@RequestParam String patientId) {
        return scheduling.listForPatient(patientId);
    }

    @PostMapping("/{id}/arrive")
    public AppointmentSummary arrive(@PathVariable String id, @RequestParam String patientId) {
        return scheduling.changeStatus(id, patientId, Appointment.AppointmentStatus.ARRIVED);
    }

    @PostMapping("/{id}/fulfill")
    public AppointmentSummary fulfill(@PathVariable String id, @RequestParam String patientId) {
        return scheduling.changeStatus(id, patientId, Appointment.AppointmentStatus.FULFILLED);
    }

    @PostMapping("/{id}/cancel")
    public AppointmentSummary cancel(@PathVariable String id, @RequestParam String patientId) {
        return scheduling.changeStatus(id, patientId, Appointment.AppointmentStatus.CANCELLED);
    }
}
