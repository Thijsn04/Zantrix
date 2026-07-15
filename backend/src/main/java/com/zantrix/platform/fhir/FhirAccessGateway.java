package com.zantrix.platform.fhir;

import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;

import java.util.List;
import java.util.Map;

/**
 * The only supported entry point for application modules to access FHIR.
 *
 * <p>Clinical operations are authorized from the current security context and
 * audited before their result is returned. The raw HAPI client stays internal
 * to this module so a capability cannot accidentally bypass those controls.
 */
public interface FhirAccessGateway {

    CapabilityStatement capabilities();

    <T extends IBaseResource> T read(Class<T> resourceType, String id, String patientId);

    Bundle search(String resourceType, Map<String, List<String>> parameters, String patientId);

    /** Fetches every search page, bounded by the platform safety limit. */
    Bundle searchAll(String resourceType, Map<String, List<String>> parameters, String patientId);

    Bundle history(String resourceType, String id, String patientId);

    Bundle transaction(Bundle bundle, String patientId);

    MethodOutcome create(IBaseResource resource, String patientId);

    MethodOutcome update(IBaseResource resource, String patientId);

    MethodOutcome delete(String resourceType, String id, String patientId);
}
