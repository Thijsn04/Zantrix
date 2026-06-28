package com.zantrix.logistics;

import com.zantrix.adt.LocationEntity;
import com.zantrix.adt.LocationRepository;
import com.zantrix.iam.AuditLoggable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController("logisticsCleaningController")
@RequestMapping("/api/v1/logistics/cleaning")
public class CleaningController {

    private final LocationRepository locationRepository;

    public CleaningController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @GetMapping("/beds")
    @PreAuthorize("hasAnyRole('NURSE', 'SYSTEM_ADMIN', 'CLEANING_STAFF')")
    @AuditLoggable
    public List<LocationEntity> getBedsNeedingCleaning() {
        return locationRepository.findAll().stream()
            .filter(loc -> loc.getLevel() == LocationEntity.Level.BED && loc.getStatus() == LocationEntity.Status.CLEANING_NEEDED)
            .toList();
    }

    @PostMapping("/beds/{bedId}/clean")
    @PreAuthorize("hasAnyRole('NURSE', 'SYSTEM_ADMIN', 'CLEANING_STAFF')")
    @AuditLoggable
    public void markBedCleaned(@PathVariable UUID bedId) {
        LocationEntity bed = locationRepository.findById(bedId).orElseThrow(() -> new IllegalArgumentException("Bed not found"));
        if (bed.getStatus() == LocationEntity.Status.CLEANING_NEEDED) {
            bed.setStatus(LocationEntity.Status.AVAILABLE);
            locationRepository.save(bed);
        }
    }
}
