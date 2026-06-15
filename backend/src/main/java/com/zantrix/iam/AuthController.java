package com.zantrix.iam;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/iam/auth")
class AuthController {

    @PostMapping("/login")
    @AuditLoggable
    public ResponseEntity<Map<String, String>> logLogin() {
        return ResponseEntity.ok(Map.of("status", "logged_in"));
    }

    @PostMapping("/logout")
    @AuditLoggable
    public ResponseEntity<Map<String, String>> logLogout() {
        return ResponseEntity.ok(Map.of("status", "logged_out"));
    }
}
