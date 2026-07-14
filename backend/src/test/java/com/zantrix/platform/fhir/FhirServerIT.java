package com.zantrix.platform.fhir;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.zantrix.IntegrationTestBase;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Enumerations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the FHIR platform end to end: a real HAPI FHIR JPA server (the
 * official image) is started, the backend's FHIR client is pointed at it, and
 * the R4 CapabilityStatement is read back. Requires Docker, so it runs in CI.
 */
@Testcontainers
class FhirServerIT extends IntegrationTestBase {

    @Container
    static final GenericContainer<?> HAPI = new GenericContainer<>("hapiproject/hapi:v8.10.0-2")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/fhir/metadata")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(5)));

    @DynamicPropertySource
    static void fhirProperties(DynamicPropertyRegistry registry) {
        registry.add("zantrix.fhir.base-url",
                () -> "http://" + HAPI.getHost() + ":" + HAPI.getMappedPort(8080) + "/fhir");
    }

    @Autowired
    IGenericClient fhirClient;

    @Test
    void backendReachesFhirServerAndReadsR4CapabilityStatement() {
        CapabilityStatement capabilities = fhirClient.capabilities()
                .ofType(CapabilityStatement.class)
                .execute();

        assertThat(capabilities.getFhirVersion()).isEqualTo(Enumerations.FHIRVersion._4_0_1);
    }
}
