package com.zantrix.platform.iam;

import com.zantrix.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Verifies that the identity endpoint resolves the session from a bearer token
 * through the central security layer, exposing roles and SMART scopes.
 */
class IamIT extends IntegrationTestBase {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void meReturnsIdentityRolesAndScopes() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("user-123")
                .claim("preferred_username", "physician_test")
                .claim("name", "Test Physician")
                .claim("realm_access", Map.of("roles", List.of("PHYSICIAN")))
                .claim("scope", "openid user/*.read")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        when(jwtDecoder.decode(eq("test-token"))).thenReturn(jwt);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("test-token");
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/iam/me", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody())
                .contains("physician_test")
                .contains("PHYSICIAN")
                .contains("user/*.read");
    }
}
