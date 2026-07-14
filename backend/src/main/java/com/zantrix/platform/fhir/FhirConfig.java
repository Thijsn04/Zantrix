package com.zantrix.platform.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring for the FHIR platform.
 *
 * <p>The canonical FHIR store and API is a dedicated HAPI FHIR JPA server (see
 * ADR 0006). The Zantrix backend is a client of that server. This configuration
 * exposes a shared R4 {@link FhirContext} and a thread safe {@link IGenericClient}
 * pointed at the configured server, so capabilities can read and write FHIR
 * resources through one place.
 */
@Configuration
public class FhirConfig {

    /**
     * The R4 context is expensive to create and is thread safe, so it is a
     * singleton shared across the application.
     */
    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR4();
    }

    @Bean
    public IGenericClient fhirClient(FhirContext fhirContext,
                                     @Value("${zantrix.fhir.base-url}") String fhirBaseUrl) {
        return fhirContext.newRestfulGenericClient(fhirBaseUrl);
    }
}
