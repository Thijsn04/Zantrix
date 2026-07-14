package com.zantrix.platform.fhir.internal;

import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CapabilityStatement;

interface FhirTransport {

    CapabilityStatement capabilities();

    <T extends IBaseResource> T read(Class<T> resourceType, String id);

    MethodOutcome create(IBaseResource resource);

    MethodOutcome update(IBaseResource resource);

    MethodOutcome delete(String resourceType, String id);

    String resourceName(Class<? extends IBaseResource> resourceType);
}
