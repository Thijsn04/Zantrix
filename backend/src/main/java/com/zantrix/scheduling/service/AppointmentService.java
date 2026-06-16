package com.zantrix.scheduling.service;

import com.zantrix.iam.AuditLoggable;
import com.zantrix.scheduling.domain.AppointmentEntity;
import com.zantrix.scheduling.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository repository;

    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    public List<AppointmentEntity> getAppointmentsForPractitioner(UUID practitionerId, OffsetDateTime start, OffsetDateTime end) {
        return repository.findByPractitionerIdAndStartTimeBetween(practitionerId, start, end);
    }

    public List<AppointmentEntity> getAppointmentsForPatient(UUID patientId) {
        return repository.findByPatientId(patientId);
    }

    public List<AppointmentEntity> checkOverlaps(UUID practitionerId, OffsetDateTime start, OffsetDateTime end) {
        return repository.findOverlappingAppointments(practitionerId, start, end);
    }

    @AuditLoggable
    public AppointmentEntity createAppointment(AppointmentEntity appointment) {
        // Overlap detection logic can go here. For now we allow double booking.
        return repository.save(appointment);
    }

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
