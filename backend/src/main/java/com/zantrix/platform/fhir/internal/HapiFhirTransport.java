package com.zantrix.platform.fhir.internal;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** Raw HAPI transport kept internal so callers must use the guarded gateway. */
@Component
class HapiFhirTransport implements FhirTransport {

    private final FhirContext fhirContext;
    private final IGenericClient client;
    private final FhirResourceValidator validator;

    HapiFhirTransport(FhirContext fhirContext, FhirResourceValidator validator,
                      @Value("${zantrix.fhir.base-url}") String baseUrl) {
        this.fhirContext = fhirContext;
        this.validator = validator;
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
    public Bundle search(String resourceType, Map<String, List<String>> parameters) {
        StringBuilder url = new StringBuilder(resourceType);
        parameters.forEach((name, values) -> values.forEach(value -> url
                .append(url.indexOf("?") < 0 ? '?' : '&')
                .append(encode(name))
                .append('=')
                .append(encode(value))));
        return client.search().byUrl(url.toString()).returnBundle(Bundle.class).execute();
    }

    @Override
    public Bundle searchAll(String resourceType, Map<String, List<String>> parameters) {
        Bundle page = search(resourceType, parameters);
        Bundle combined = new Bundle().setType(Bundle.BundleType.SEARCHSET);
        int pages = 0;
        while (true) {
            combined.getEntry().addAll(page.getEntry());
            pages++;
            if (combined.getEntry().size() > 100_000 || pages > 1_000) {
                throw new IllegalStateException("FHIR search exceeded the safe pagination limit");
            }
            if (page.getLink(Bundle.LINK_NEXT) == null) {
                break;
            }
            page = client.loadPage().next(page).execute();
        }
        combined.setTotal(combined.getEntry().size());
        return combined;
    }

    @Override
    public Bundle history(String resourceType, String id) {
        return client.history().onInstance(resourceType + "/" + id).returnBundle(Bundle.class).execute();
    }

    @Override
    public Bundle transaction(Bundle bundle) {
        bundle.getEntry().stream().map(Bundle.BundleEntryComponent::getResource)
                .filter(java.util.Objects::nonNull).forEach(validator::validate);
        return client.transaction().withBundle(bundle).execute();
    }

    @Override
    public MethodOutcome create(IBaseResource resource) {
        validator.validate(resource);
        if (resource.getIdElement() != null && resource.getIdElement().hasIdPart()) {
            return client.update().resource(resource).execute();
        }
        return client.create().resource(resource).execute();
    }

    @Override
    public MethodOutcome update(IBaseResource resource) {
        validator.validate(resource);
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

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
