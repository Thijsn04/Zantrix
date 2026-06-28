package com.zantrix.kiosk;

import com.zantrix.iam.AuditLoggable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import com.zantrix.scheduling.service.AppointmentService;
import com.zantrix.scheduling.domain.AppointmentEntity;
import com.zantrix.scheduling.domain.AppointmentStatus;

@RestController
@RequestMapping("/api/v1/kiosk")
public class KioskController {

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('PATIENT', 'SYSTEM_ADMIN')")
    @AuditLoggable(patientIdParam = "patientId")
    public ResponseEntity<Map<String, Object>> checkIn(@RequestParam UUID patientId) {
        List<AppointmentEntity> appointments = appointmentService.getAppointmentsForPatient(patientId);
        
        Map<String, Object> response = new HashMap<>();
        
        // Find the first BOOKED appointment for today (or just the first BOOKED one for simplicity)
        AppointmentEntity currentAppointment = appointments.stream()
                .filter(a -> AppointmentStatus.BOOKED.equals(a.getStatus()))
                .findFirst()
                .orElse(null);
                
        if (currentAppointment != null) {
            currentAppointment.setStatus(AppointmentStatus.ARRIVED);
            appointmentService.updateAppointment(currentAppointment.getId(), currentAppointment);
            response.put("status", "SUCCESS");
            response.put("message", "Je bent succesvol aangemeld. Neem plaats in wachtruimte B.");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "NOT_FOUND");
            response.put("message", "Geen geplande afspraken gevonden om aan te melden.");
            return ResponseEntity.status(404).body(response);
        }
    }
    
    private final com.zantrix.pmi.repository.PatientRepository patientRepository;
    private final QuestionnaireResponseRepository responseRepository;
    private final AppointmentService appointmentService;
    
    public KioskController(com.zantrix.pmi.repository.PatientRepository patientRepository, 
                           QuestionnaireResponseRepository responseRepository,
                           AppointmentService appointmentService) {
        this.patientRepository = patientRepository;
        this.responseRepository = responseRepository;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/verify-id")
    @PreAuthorize("hasAnyRole('PATIENT', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> verifyIdDocument(@RequestParam String documentNumber) {
        // Real verification against the Patient Master Index (PMI)
        return patientRepository.findByBsn(documentNumber)
            .map(patient -> {
                Map<String, Object> response = new HashMap<>();
                response.put("verified", true);
                response.put("patientId", patient.getId());
                return ResponseEntity.ok(response);
            })
            .orElseGet(() -> {
                Map<String, Object> response = new HashMap<>();
                response.put("verified", false);
                return ResponseEntity.status(404).body(response);
            });
    }

    @PostMapping("/responses")
    @PreAuthorize("hasAnyRole('PATIENT', 'SYSTEM_ADMIN')")
    @AuditLoggable
    public ResponseEntity<QuestionnaireResponseEntity> submitResponse(@RequestBody QuestionnaireResponseEntity response) {
        return ResponseEntity.ok(responseRepository.save(response));
    }
}
