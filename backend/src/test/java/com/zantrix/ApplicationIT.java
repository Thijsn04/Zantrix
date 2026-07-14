package com.zantrix;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke tests for the platform foundation: the context boots against a real
 * database, the health probe is public, and application endpoints require
 * authentication.
 */
class ApplicationIT extends IntegrationTestBase {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void healthProbeIsPublicAndReportsUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    void systemEndpointRequiresAuthentication() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/system/info", String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }
}
