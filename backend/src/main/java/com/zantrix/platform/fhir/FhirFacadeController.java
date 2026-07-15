package com.zantrix.platform.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Secured R4 facade. The internal HAPI endpoint must never be internet exposed. */
@RestController
@RequestMapping("/fhir/R4")
public class FhirFacadeController {
    private static final MediaType FHIR_JSON = MediaType.parseMediaType("application/fhir+json");
    private static final Set<String> SUPPORTED = Set.of(
            "Patient", "Practitioner", "PractitionerRole", "Organization", "Location",
            "Encounter", "Appointment", "Schedule", "Slot", "Condition", "AllergyIntolerance",
            "MedicationRequest", "MedicationStatement", "MedicationAdministration", "MedicationDispense",
            "ServiceRequest", "Observation", "DiagnosticReport", "Composition", "DocumentReference",
            "Consent", "Task", "DetectedIssue", "AuditEvent", "Provenance");

    private final FhirAccessGateway fhir;
    private final FhirContext context;

    public FhirFacadeController(FhirAccessGateway fhir, FhirContext context) {
        this.fhir = fhir;
        this.context = context;
    }

    @GetMapping(value = "/metadata", produces = "application/fhir+json")
    String metadata() {
        return encode(fhir.capabilities());
    }

    @GetMapping(value = "/{type}", produces = "application/fhir+json")
    String search(@PathVariable String type, @RequestParam MultiValueMap<String, String> parameters,
                  @RequestHeader(value = "X-Patient-Context", required = false) String patientContext) {
        requireSupported(type);
        return encode(fhir.search(type, Map.copyOf(parameters), patientFrom(parameters, patientContext)));
    }

    @GetMapping(value = "/{type}/{id}", produces = "application/fhir+json")
    String read(@PathVariable String type, @PathVariable String id,
                @RequestHeader(value = "X-Patient-Context", required = false) String patientContext) {
        requireSupported(type);
        String patientId = "Patient".equals(type) ? id : requiredPatientContext(patientContext);
        return encode(fhir.read(resourceClass(type), id, patientId));
    }

    @GetMapping(value = "/{type}/{id}/_history", produces = "application/fhir+json")
    String history(@PathVariable String type, @PathVariable String id,
                   @RequestHeader(value = "X-Patient-Context", required = false) String patientContext) {
        requireSupported(type);
        String patientId = "Patient".equals(type) ? id : requiredPatientContext(patientContext);
        return encode(fhir.history(type, id, patientId));
    }

    @PostMapping(consumes = "application/fhir+json", produces = "application/fhir+json")
    String transaction(@RequestBody String body,
                       @RequestHeader(value = "X-Patient-Context") String patientContext) {
        IBaseResource resource = parse(body);
        if (!(resource instanceof Bundle bundle)) {
            throw new IllegalArgumentException("FHIR endpoint root accepts only a transaction Bundle");
        }
        return encode(fhir.transaction(bundle, requiredPatientContext(patientContext)));
    }

    @PostMapping(value = "/{type}", consumes = "application/fhir+json", produces = "application/fhir+json")
    ResponseEntity<String> create(@PathVariable String type, @RequestBody String body,
                                  @RequestHeader(value = "X-Patient-Context", required = false) String patientContext) {
        requireSupported(type);
        IBaseResource resource = parseType(body, type);
        MethodOutcome outcome = fhir.create(resource, patientContext(type, resource, patientContext));
        String location = outcome.getId() == null ? null : "/fhir/R4/" + outcome.getId().toUnqualifiedVersionless();
        ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.CREATED).contentType(FHIR_JSON);
        if (location != null) response.location(URI.create(location));
        return response.body(outcome.getResource() == null ? encode(resource) : encode(outcome.getResource()));
    }

    @PutMapping(value = "/{type}/{id}", consumes = "application/fhir+json", produces = "application/fhir+json")
    ResponseEntity<String> update(@PathVariable String type, @PathVariable String id, @RequestBody String body,
                                  @RequestHeader(value = "X-Patient-Context", required = false) String patientContext) {
        requireSupported(type);
        IBaseResource resource = parseType(body, type);
        resource.setId(type + "/" + id);
        MethodOutcome outcome = fhir.update(resource, patientContext(type, resource, patientContext));
        ResponseEntity.BodyBuilder response = ResponseEntity.ok().contentType(FHIR_JSON);
        if (outcome.getId() != null && outcome.getId().getVersionIdPart() != null) {
            response.header(HttpHeaders.ETAG, "W/\"" + outcome.getId().getVersionIdPart() + "\"");
        }
        return response.body(outcome.getResource() == null ? encode(resource) : encode(outcome.getResource()));
    }

    @DeleteMapping("/{type}/{id}")
    ResponseEntity<Void> delete(@PathVariable String type, @PathVariable String id,
                                @RequestHeader(value = "X-Patient-Context", required = false) String patientContext) {
        requireSupported(type);
        fhir.delete(type, id, "Patient".equals(type) ? id : requiredPatientContext(patientContext));
        return ResponseEntity.noContent().build();
    }

    @SuppressWarnings("unchecked")
    private Class<IBaseResource> resourceClass(String type) {
        return (Class<IBaseResource>) context.getResourceDefinition(type).getImplementingClass();
    }

    private IBaseResource parse(String body) {
        return context.newJsonParser().parseResource(body);
    }

    private IBaseResource parseType(String body, String type) {
        IBaseResource resource = parse(body);
        if (!type.equals(resource.fhirType())) {
            throw new IllegalArgumentException("Resource type does not match request path");
        }
        return resource;
    }

    private String encode(IBaseResource resource) {
        return context.newJsonParser().setPrettyPrint(false).encodeResourceToString(resource);
    }

    private static void requireSupported(String type) {
        if (!SUPPORTED.contains(type)) throw new IllegalArgumentException("Unsupported FHIR resource type");
    }

    private static String patientFrom(MultiValueMap<String, String> parameters, String header) {
        if (header != null && !header.isBlank()) return header;
        for (String key : List.of("patient", "subject", "individual", "beneficiary")) {
            String value = parameters.getFirst(key);
            if (value != null && !value.isBlank()) return value.replace("Patient/", "");
        }
        return null;
    }

    private static String patientContext(String type, IBaseResource resource, String header) {
        if ("Patient".equals(type)) {
            String id = resource.getIdElement().getIdPart();
            return id == null || id.isBlank() ? header : id;
        }
        return requiredPatientContext(header);
    }

    private static String requiredPatientContext(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("X-Patient-Context is required for this clinical operation");
        }
        return value;
    }
}
