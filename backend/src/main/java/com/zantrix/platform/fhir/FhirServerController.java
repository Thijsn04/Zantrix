package com.zantrix.platform.fhir;

import org.hl7.fhir.r4.model.CapabilityStatement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reports connectivity to the FHIR server.
 *
 * <p>Authenticated observability endpoint that confirms the backend can reach
 * the dedicated HAPI FHIR server and reports its FHIR version and software. It
 * degrades gracefully when the server is unreachable rather than failing.
 */
@RestController
@RequestMapping("/api/v1/fhir")
public class FhirServerController {

    private final FhirAccessGateway fhirAccess;
    private final String fhirBaseUrl;

    public FhirServerController(FhirAccessGateway fhirAccess,
                                @Value("${zantrix.fhir.base-url}") String fhirBaseUrl) {
        this.fhirAccess = fhirAccess;
        this.fhirBaseUrl = fhirBaseUrl;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("baseUrl", fhirBaseUrl);
        try {
            CapabilityStatement capabilities = fhirAccess.capabilities();
            result.put("reachable", true);
            result.put("fhirVersion", capabilities.getFhirVersion().toCode());
            result.put("software", capabilities.getSoftware().getName());
        } catch (Exception e) {
            result.put("reachable", false);
        }
        return result;
    }
}
