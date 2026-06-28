package com.zantrix.logistics;

import com.zantrix.iam.AuditLoggable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import java.time.OffsetDateTime;

@RestController("logisticsTransportController")
@RequestMapping("/api/v1/logistics")
public class TransportController {

    private final TransportJobRepository repository;

    public TransportController(TransportJobRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/transport/jobs")
    @PreAuthorize("hasAnyRole('NURSE', 'SYSTEM_ADMIN', 'PORTER')")
    @AuditLoggable
    public List<TransportJobEntity> getActiveJobs() {
        return repository.findByStatus(TransportJobEntity.Status.PENDING);
    }

    @PostMapping("/transport/jobs")
    @PreAuthorize("hasAnyRole('PRACTITIONER', 'NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable(patientIdParam = "patientId")
    public TransportJobEntity requestTransport(@RequestBody TransportJobEntity job) {
        job.setStatus(TransportJobEntity.Status.PENDING);
        job.setRequestedTime(OffsetDateTime.now());
        return repository.save(job);
    }
}
