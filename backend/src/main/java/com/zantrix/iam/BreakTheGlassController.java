package com.zantrix.iam;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/iam/break-the-glass")
class BreakTheGlassController {

    private final BreakTheGlassSessionRepository repository;

    public BreakTheGlassController(BreakTheGlassSessionRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    @AuditLoggable
    public ResponseEntity<Map<String, String>> activateBreakTheGlass(
            @RequestBody BreakTheGlassRequest request, 
            Authentication authentication) {
        
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Reason is required"));
        }
        
        BreakTheGlassSession session = new BreakTheGlassSession(
                authentication.getName(),
                request.getReason(),
                LocalDateTime.now().plusHours(4)
        );
        repository.save(session);
        
        return ResponseEntity.ok(Map.of("message", "Emergency access granted for 4 hours."));
    }
}
