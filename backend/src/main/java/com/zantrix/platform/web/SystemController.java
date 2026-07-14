package com.zantrix.platform.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Minimal authenticated system endpoint.
 *
 * <p>Confirms the caller is authenticated and reports basic application
 * metadata. It is a placeholder surface for the platform module until the
 * capabilities above it are built.
 */
@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    private final String applicationName;

    public SystemController(@Value("${spring.application.name}") String applicationName) {
        this.applicationName = applicationName;
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of(
                "application", applicationName,
                "status", "ok");
    }
}
