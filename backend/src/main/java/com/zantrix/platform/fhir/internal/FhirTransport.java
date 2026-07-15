package com.zantrix.platform.fhir.internal;

import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;

import java.util.List;
import java.util.Map;

interface FhirTransport {

    CapabilityStatement capabilities();

    <T extends IBaseResource> T read(Class<T> resourceType, String id);

    Bundle search(String resourceType, Map<String, List<String>> parameters);

    Bundle searchAll(String resourceType, Map<String, List<String>> parameters);

    Bundle history(String resourceType, String id);

    Bundle transaction(Bundle bundle);

    MethodOutcome create(IBaseResource resource);

    MethodOutcome update(IBaseResource resource);

    MethodOutcome delete(String resourceType, String id);

    String resourceName(Class<? extends IBaseResource> resourceType);
}
