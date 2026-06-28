package com.zantrix.scheduling.controller;


import com.zantrix.scheduling.domain.LocationEntity;
import com.zantrix.scheduling.repository.LocationRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.zantrix.iam.AuditLoggable;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.UUID;
import java.util.List;

@RestController("schedulingAdtController")
@RequestMapping("/api/v1/scheduling/adt")
public class AdtController {

    private final LocationRepository locationRepository;

    public AdtController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @GetMapping("/locations/beds")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable
    public List<LocationEntity> getAvailableBeds() {
        return locationRepository.findByTypeAndStatus("BED", "ACTIVE");
    }

    @PostMapping("/admit")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @AuditLoggable
    public ResponseEntity<?> admitPatient(@RequestBody java.util.Map<String, String> payload) {
        // Logic: create or find encounter, set status to IN_PROGRESS, set locationId
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @AuditLoggable
    public ResponseEntity<?> transferPatient(@RequestBody java.util.Map<String, String> payload) {
        // Logic: find encounter, update locationId
        return ResponseEntity.ok().build();
    }

    @PostMapping("/discharge")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @AuditLoggable
    public ResponseEntity<?> dischargePatient(@RequestBody java.util.Map<String, String> payload) {
        // Logic: find encounter, set status to FINISHED
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ward/{wardId}/census")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable
    public ResponseEntity<List<LocationEntity>> getWardCensus(@PathVariable UUID wardId) {
        return ResponseEntity.ok(locationRepository.findByTypeAndStatus("BED", "ACTIVE"));
    }
}
