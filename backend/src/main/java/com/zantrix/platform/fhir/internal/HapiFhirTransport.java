package com.zantrix.platform.fhir.internal;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Raw HAPI transport kept internal so callers must use the guarded gateway. */
@Component
class HapiFhirTransport implements FhirTransport {

    private final FhirContext fhirContext;
    private final IGenericClient client;

    HapiFhirTransport(FhirContext fhirContext, @Value("${zantrix.fhir.base-url}") String baseUrl) {
        this.fhirContext = fhirContext;
        this.client = fhirContext.newRestfulGenericClient(baseUrl);
    }

    @Override
    public CapabilityStatement capabilities() {
        return client.capabilities().ofType(CapabilityStatement.class).execute();
    }

    @Override
    public <T extends IBaseResource> T read(Class<T> resourceType, String id) {
        return client.read().resource(resourceType).withId(id).execute();
    }

    @Override
    public MethodOutcome create(IBaseResource resource) {
        return client.create().resource(resource).execute();
    }

    @Override
    public MethodOutcome update(IBaseResource resource) {
        return client.update().resource(resource).execute();
    }

    @Override
    public MethodOutcome delete(String resourceType, String id) {
        return client.delete().resourceById(resourceType, id).execute();
    }

    @Override
    public String resourceName(Class<? extends IBaseResource> resourceType) {
        return fhirContext.getResourceType(resourceType);
    }
}
