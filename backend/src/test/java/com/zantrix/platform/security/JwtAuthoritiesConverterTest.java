package com.zantrix.platform.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthoritiesConverterTest {

    private final JwtAuthoritiesConverter converter = new JwtAuthoritiesConverter();

    @Test
    void mapsRealmRolesAndSpaceDelimitedScopes() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("realm_access", Map.of("roles", List.of("PHYSICIAN", "NURSE")))
                .claim("scope", "openid patient/Observation.read")
                .build();

        List<String> authorities = converter.convert(jwt).stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertThat(authorities).contains(
                "ROLE_PHYSICIAN",
                "ROLE_NURSE",
                "SCOPE_openid",
                "SCOPE_patient/Observation.read");
    }

    @Test
    void supportsScpArrayClaim() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("scp", List.of("user/*.read"))
                .build();

        List<String> authorities = converter.convert(jwt).stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertThat(authorities).contains("SCOPE_user/*.read");
    }

    @Test
    void returnsNoAuthoritiesWhenClaimsAbsent() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user")
                .build();

        assertThat(converter.convert(jwt)).isEmpty();
    }
}
