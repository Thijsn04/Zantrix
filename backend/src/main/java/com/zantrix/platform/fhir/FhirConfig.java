package com.zantrix.platform.fhir;

import ca.uhn.fhir.context.FhirContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring for the FHIR platform.
 *
 * <p>The canonical FHIR store and API is a dedicated HAPI FHIR JPA server (see
 * ADR 0006). The Zantrix backend is a client of that server. This configuration
 * exposes a shared R4 {@link FhirContext}. The raw HAPI client is kept inside
 * the FHIR module so capabilities cannot bypass the authorized and audited
 * {@link FhirAccessGateway}.
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
}
