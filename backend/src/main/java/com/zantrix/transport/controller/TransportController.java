package com.zantrix.transport.controller;

import com.zantrix.transport.domain.TransportRequestEntity;
import com.zantrix.transport.repository.TransportRequestRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

import com.zantrix.iam.AuditLoggable;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController("coreTransportController")
@RequestMapping("/api/v1/transport")
public class TransportController {
    
    private final TransportRequestRepository repository;

    public TransportController(TransportRequestRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/requests")
    @PreAuthorize("hasAnyRole('NURSE', 'SYSTEM_ADMIN')") // Porters/Transport roles could be added here
    @AuditLoggable
    public List<TransportRequestEntity> getRequests() {
        return repository.findAll();
    }

    @PostMapping("/dispatch")
    @PreAuthorize("hasAnyRole('NURSE', 'SYSTEM_ADMIN')")
    @AuditLoggable
    public ResponseEntity<TransportRequestEntity> dispatchTransport(@RequestBody TransportRequestEntity request) {
        request.setStatus("DISPATCHED");
        return ResponseEntity.ok(repository.save(request));
    }
}
