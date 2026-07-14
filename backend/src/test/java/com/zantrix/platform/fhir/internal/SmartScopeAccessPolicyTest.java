package com.zantrix.platform.fhir.internal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmartScopeAccessPolicyTest {

    private final SmartScopeAccessPolicy policy = new SmartScopeAccessPolicy();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void userWildcardReadScopeAllowsPatientRead() {
        authenticate("SCOPE_user/*.read", null);

        assertThatCode(() -> policy.authorize(FhirOperation.READ, "Patient", "p1", "p1"))
                .doesNotThrowAnyException();
    }

    @Test
    void smartV2PermissionsAreAppliedPerInteraction() {
        authenticate("SCOPE_user/Patient.cru", null);

        assertThatCode(() -> policy.authorize(FhirOperation.CREATE, "Patient", "p1", "p1"))
                .doesNotThrowAnyException();
        assertThatCode(() -> policy.authorize(FhirOperation.UPDATE, "Patient", "p1", "p1"))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.authorize(FhirOperation.DELETE, "Patient", "p1", "p1"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void patientScopeIsLimitedToTheTokensOwnPatientResource() {
        authenticate("SCOPE_patient/*.read", "p1");

        assertThatCode(() -> policy.authorize(FhirOperation.READ, "Patient", "p1", "p1"))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.authorize(FhirOperation.READ, "Patient", "p2", "p1"))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> policy.authorize(FhirOperation.READ, "Observation", "o1", "p1"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void scopeForAnotherResourceIsRejected() {
        authenticate("SCOPE_user/Observation.read", null);

        assertThatThrownBy(() -> policy.authorize(FhirOperation.READ, "Patient", "p1", "p1"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void malformedPermissionDoesNotAccidentallyGrantAccess() {
        authenticate("SCOPE_user/Patient.trash", null);

        assertThatThrownBy(() -> policy.authorize(FhirOperation.READ, "Patient", "p1", "p1"))
                .isInstanceOf(AccessDeniedException.class);
    }

    private void authenticate(String authority, String patientId) {
        Jwt.Builder jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-1");
        if (patientId != null) {
            jwt.claim("patient", patientId);
        }
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
                jwt.build(), List.of(new SimpleGrantedAuthority(authority))));
    }
}
