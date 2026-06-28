package com.zantrix.cleaning.controller;

import com.zantrix.cleaning.domain.CleaningTaskEntity;
import com.zantrix.cleaning.repository.CleaningTaskRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.UUID;
import java.time.OffsetDateTime;
import com.zantrix.iam.AuditLoggable;
import org.springframework.security.access.prepost.PreAuthorize;
import com.zantrix.adt.LocationRepository;
import com.zantrix.adt.LocationEntity;

@RestController("coreCleaningController")
@RequestMapping("/api/v1/cleaning")
public class CleaningController {
    
    private final CleaningTaskRepository repository;
    private final LocationRepository locationRepository;

    public CleaningController(CleaningTaskRepository repository, LocationRepository locationRepository) {
        this.repository = repository;
        this.locationRepository = locationRepository;
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasAnyRole('NURSE', 'SYSTEM_ADMIN')") // Environmental services could be added here
    @AuditLoggable
    public List<CleaningTaskEntity> getTasks() {
        return repository.findAll();
    }

    @PostMapping("/tasks/{id}/complete")
    @PreAuthorize("hasAnyRole('NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable
    public ResponseEntity<?> completeTask(@PathVariable UUID id) {
        CleaningTaskEntity task = repository.findById(id).orElseThrow();
        task.setStatus("COMPLETED");
        task.setCompletedAt(OffsetDateTime.now());
        
        locationRepository.findById(task.getLocationId()).ifPresent(location -> {
            if (location.getStatus() == LocationEntity.Status.CLEANING_NEEDED) {
                location.setStatus(LocationEntity.Status.AVAILABLE);
                locationRepository.save(location);
            }
        });
        
        repository.save(task);
        return ResponseEntity.ok().build();
    }
}
