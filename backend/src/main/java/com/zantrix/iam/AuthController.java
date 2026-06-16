package com.zantrix.iam;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * REST Controller for capturing authentication events.
 * <p>
 * Provides endpoints strictly for auditing login and logout actions, ensuring access events
 * are integrated into the compliance logging system (MDR/NEN7510). Actual authentication
 * logic is handled externally by Keycloak/OAuth2.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/iam/auth")
class AuthController {

    /**
     * Endpoint to explicitly log a user login event.
     *
     * @return A response confirming the login status.
     */
    @PostMapping("/login")
    @AuditLoggable
    public ResponseEntity<Map<String, String>> logLogin() {
        return ResponseEntity.ok(Map.of("status", "logged_in"));
    }

    /**
     * Endpoint to explicitly log a user logout event.
     *
     * @return A response confirming the logout status.
     */
    @PostMapping("/logout")
    @AuditLoggable
    public ResponseEntity<Map<String, String>> logLogout() {
        return ResponseEntity.ok(Map.of("status", "logged_out"));
    }
}
