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

/**
 * REST Controller for handling Break-the-Glass emergency access requests.
 * <p>
 * This controller allows authorized personnel (e.g., DOCTOR, NURSE) to elevate their privileges
 * temporarily in emergency situations, ensuring critical patient data is accessible while maintaining
 * strict audit logging required by MDR and NEN7510 standards.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/iam/break-the-glass")
class BreakTheGlassController {

    private final BreakTheGlassSessionRepository repository;

    /**
     * Constructs a new {@link BreakTheGlassController}.
     *
     * @param repository The repository for break-the-glass sessions.
     */
    public BreakTheGlassController(BreakTheGlassSessionRepository repository) {
        this.repository = repository;
    }

    /**
     * Activates a break-the-glass session, granting temporary emergency access.
     * <p>
     * All activations are strictly audited. A valid reason must be provided.
     * </p>
     *
     * @param request The request payload containing the reason for emergency access.
     * @param authentication The current user's authentication details.
     * @return A response entity containing the status message or an error.
     */
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    @AuditLoggable
    public ResponseEntity<Map<String, String>> activateBreakTheGlass(
            @RequestBody BreakTheGlassRequest request, 
            Authentication authentication) {
        
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Reason is required"));
        }
        
        if (request.getPatientId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "patientId is required"));
        }
        
        BreakTheGlassSession session = new BreakTheGlassSession(
                authentication.getName(),
                request.getReason(),
                request.getPatientId(),
                LocalDateTime.now().plusHours(4)
        );
        repository.save(session);
        
        return ResponseEntity.ok(Map.of("message", "Emergency access granted for 4 hours."));
    }
}
