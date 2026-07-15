package com.zantrix.terminology.internal;

import ca.uhn.fhir.context.FhirContext;
import com.zantrix.terminology.TerminologyUnavailableException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class SnowstormClient {

    private final FhirContext fhirContext;
    private final RestClient client;
    private final java.net.URI statusUri;

    SnowstormClient(FhirContext fhirContext, @Value("${zantrix.terminology.base-url}") String baseUrl) {
        this.fhirContext = fhirContext;
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.statusUri = java.net.URI.create(baseUrl).resolve("/version");
    }

    java.util.Map<String, Object> status() {
        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> status = client.get().uri(statusUri)
                    .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                    .retrieve().body(java.util.Map.class);
            return status == null ? java.util.Map.of() : status;
        } catch (RuntimeException failure) {
            throw new TerminologyUnavailableException(failure);
        }
    }

    ValueSet expand(String url, String filter, int count, String displayLanguage) {
        return get(ValueSet.class, uri -> uri.path("/ValueSet/$expand")
                .queryParam("url", url)
                .queryParamIfPresent("filter", optional(filter))
                .queryParam("count", count)
                .queryParamIfPresent("displayLanguage", optional(displayLanguage))
                .build());
    }

    Parameters validateCode(String system, String code, String display) {
        return get(Parameters.class, uri -> uri.path("/CodeSystem/$validate-code")
                .queryParam("url", system)
                .queryParam("code", code)
                .queryParamIfPresent("display", optional(display))
                .build());
    }

    private <T extends IBaseResource> T get(Class<T> type, String path) {
        return get(type, uri -> uri.path(path).build());
    }

    private <T extends IBaseResource> T get(Class<T> type,
                                            java.util.function.Function<org.springframework.web.util.UriBuilder,
                                                    java.net.URI> uri) {
        try {
            String body = client.get().uri(uri).accept(org.springframework.http.MediaType.APPLICATION_JSON)
                    .retrieve().body(String.class);
            return type.cast(fhirContext.newJsonParser().parseResource(body));
        } catch (RuntimeException failure) {
            throw new TerminologyUnavailableException(failure);
        }
    }

    private static java.util.Optional<String> optional(String value) {
        return value == null || value.isBlank() ? java.util.Optional.empty() : java.util.Optional.of(value);
    }
}
