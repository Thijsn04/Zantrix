package com.zantrix.terminology.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/terminology/admin")
public class AdminTerminologyController {

    @PostMapping("/snomed")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, String>> importSnomed(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("status", "Success", "message", "Please use the HAPI FHIR /fhir/$upload-external-code-system endpoint for SNOMED CT."));
    }

    @PostMapping("/loinc")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, String>> importLoinc(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("status", "Success", "message", "Please use the HAPI FHIR /fhir/$upload-external-code-system endpoint for LOINC."));
    }

    @PostMapping("/custom-codesystem")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, String>> importCustomCodeSystem(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("status", "Success", "message", "Please POST the CodeSystem JSON directly to /fhir/CodeSystem."));
    }
}
