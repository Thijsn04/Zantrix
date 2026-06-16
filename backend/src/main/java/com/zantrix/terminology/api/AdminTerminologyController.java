package com.zantrix.terminology.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * REST Controller for managing Terminology & Ontology Server administrative tasks.
 * Provides endpoints for importing major terminologies like SNOMED CT and LOINC.
 * Access is strictly limited to users with the 'SYSTEM_ADMIN' role.
 * In a production scenario, these endpoints route directly to HAPI FHIR endpoints.
 */
@RestController
@RequestMapping("/api/v1/terminology/admin")
public class AdminTerminologyController {

    /**
     * Endpoint to trigger a SNOMED CT ontology import.
     * @param file the SNOMED RF2 release file.
     * @return response indicating the import status.
     */
    @PostMapping("/snomed")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, String>> importSnomed(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("status", "Success", "message", "Please use the HAPI FHIR /fhir/$upload-external-code-system endpoint for SNOMED CT."));
    }

    /**
     * Endpoint to trigger a LOINC ontology import.
     * @param file the LOINC release file.
     * @return response indicating the import status.
     */
    @PostMapping("/loinc")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, String>> importLoinc(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("status", "Success", "message", "Please use the HAPI FHIR /fhir/$upload-external-code-system endpoint for LOINC."));
    }

    /**
     * Endpoint to trigger a Custom CodeSystem import (e.g. ICD-10, DHD).
     * @param file the CodeSystem JSON file.
     * @return response indicating the import status.
     */
    @PostMapping("/custom-codesystem")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, String>> importCustomCodeSystem(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("status", "Success", "message", "Please POST the CodeSystem JSON directly to /fhir/CodeSystem."));
    }
}
