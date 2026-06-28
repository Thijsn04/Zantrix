package com.zantrix.adt;

import com.zantrix.iam.AuditLoggable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController("coreAdtController")
@RequestMapping("/api/v1/adt")
public class AdtController {

    private final LocationRepository locationRepository;

    public AdtController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @PostMapping("/admit")
    @PreAuthorize("hasAnyRole('PRACTITIONER', 'NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable(patientIdParam = "patientId")
    public void admitPatient(@RequestParam UUID patientId, @RequestParam UUID bedId) {
        LocationEntity bed = locationRepository.findById(bedId).orElseThrow(() -> new IllegalArgumentException("Bed not found"));
        if (bed.getStatus() != LocationEntity.Status.AVAILABLE) {
            throw new IllegalArgumentException("Bed is not available");
        }
        bed.setStatus(LocationEntity.Status.OCCUPIED);
        locationRepository.save(bed);
        // Additional encounter creation logic would go here
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('PRACTITIONER', 'NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable(patientIdParam = "patientId")
    public void transferPatient(@RequestParam UUID patientId, @RequestParam UUID oldBedId, @RequestParam UUID newBedId) {
        LocationEntity oldBed = locationRepository.findById(oldBedId).orElseThrow();
        LocationEntity newBed = locationRepository.findById(newBedId).orElseThrow();
        
        oldBed.setStatus(LocationEntity.Status.CLEANING_NEEDED);
        newBed.setStatus(LocationEntity.Status.OCCUPIED);
        
        locationRepository.saveAll(List.of(oldBed, newBed));
    }

    @PostMapping("/discharge")
    @PreAuthorize("hasAnyRole('PRACTITIONER', 'NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable(patientIdParam = "patientId")
    public void dischargePatient(@RequestParam UUID patientId, @RequestParam UUID bedId) {
        LocationEntity bed = locationRepository.findById(bedId).orElseThrow();
        bed.setStatus(LocationEntity.Status.CLEANING_NEEDED);
        locationRepository.save(bed);
    }

    @GetMapping("/ward/{wardId}/census")
    @PreAuthorize("hasAnyRole('PRACTITIONER', 'NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable
    public List<LocationEntity> getWardCensus(@PathVariable UUID wardId) {
        // Find all beds belonging to this ward
        return locationRepository.findByParentIdAndLevel(wardId, LocationEntity.Level.BED);
    }

    @GetMapping("/wards")
    @PreAuthorize("hasAnyRole('PRACTITIONER', 'NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable
    public List<LocationEntity> getWards() {
        return locationRepository.findByLevel(LocationEntity.Level.WARD);
    }
}
