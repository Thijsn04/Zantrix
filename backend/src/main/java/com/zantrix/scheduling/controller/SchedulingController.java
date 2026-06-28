package com.zantrix.scheduling.controller;

import com.zantrix.scheduling.domain.WaitlistEntity;
import com.zantrix.scheduling.domain.SlotEntity;
import com.zantrix.scheduling.repository.WaitlistRepository;
import com.zantrix.scheduling.repository.SlotRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import com.zantrix.iam.AuditLoggable;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/scheduling")
public class SchedulingController {
    
    private final WaitlistRepository waitlistRepository;
    private final SlotRepository slotRepository;

    public SchedulingController(WaitlistRepository waitlistRepository, SlotRepository slotRepository) {
        this.waitlistRepository = waitlistRepository;
        this.slotRepository = slotRepository;
    }

    @PostMapping("/waitlist")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable
    public ResponseEntity<WaitlistEntity> addToWaitlist(@RequestBody WaitlistEntity waitlist) {
        return ResponseEntity.ok(waitlistRepository.save(waitlist));
    }

    @GetMapping("/slots/find")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable
    public List<SlotEntity> findSlots(@RequestParam UUID practitionerId) {
        return slotRepository.findByPractitionerIdAndStatus(practitionerId, "FREE");
    }
}
