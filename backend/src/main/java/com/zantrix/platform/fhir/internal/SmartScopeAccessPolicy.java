package com.zantrix.platform.fhir.internal;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** Applies SMART on FHIR resource scopes to every clinical FHIR operation. */
@Component
class SmartScopeAccessPolicy implements FhirAccessPolicy {

    @Override
    public void authorize(FhirOperation operation, String resourceType, String resourceId, String patientId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("FHIR access requires authentication");
        }

        boolean allowed = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("SCOPE_"))
                .map(authority -> authority.substring("SCOPE_".length()))
                .anyMatch(scope -> permits(scope, operation, resourceType, resourceId, patientId, authentication));

        if (!allowed) {
            throw new AccessDeniedException("The token does not grant the required FHIR scope");
        }
    }

    private boolean permits(String scope, FhirOperation operation, String resourceType,
                            String resourceId, String patientId, Authentication authentication) {
        int slash = scope.indexOf('/');
        int dot = scope.lastIndexOf('.');
        if (slash <= 0 || dot <= slash + 1 || dot == scope.length() - 1) {
            return false;
        }

        String context = scope.substring(0, slash).toLowerCase(Locale.ROOT);
        String scopedResource = scope.substring(slash + 1, dot);
        String permission = scope.substring(dot + 1).toLowerCase(Locale.ROOT);

        if (!context.equals("patient") && !context.equals("user") && !context.equals("system")) {
            return false;
        }
        if (!scopedResource.equals("*") && !scopedResource.equals(resourceType)) {
            return false;
        }
        if (context.equals("patient")
                && !permitsPatientSelfAccess(authentication, resourceType, resourceId, patientId)) {
            return false;
        }
        return grants(permission, operation);
    }

    private boolean permitsPatientSelfAccess(Authentication authentication, String resourceType,
                                             String resourceId, String patientId) {
        if (!"Patient".equals(resourceType)
                || resourceId == null
                || patientId == null
                || !(authentication instanceof JwtAuthenticationToken token)) {
            return false;
        }
        Jwt jwt = token.getToken();
        String tokenPatient = jwt.getClaimAsString("patient");
        return patientId.equals(tokenPatient) && resourceId.equals(tokenPatient);
    }

    private boolean grants(String permission, FhirOperation operation) {
        if (permission.equals("read")) {
            return isRead(operation);
        }
        if (permission.equals("write")) {
            return isWrite(operation);
        }
        if (!permission.matches("[cruds]+")) {
            return false;
        }
        return switch (operation) {
            case READ, SEARCH, HISTORY -> permission.indexOf('r') >= 0;
            case CREATE -> permission.indexOf('c') >= 0;
            case UPDATE -> permission.indexOf('u') >= 0;
            case DELETE -> permission.indexOf('d') >= 0;
            case TRANSACTION -> false;
        };
    }

    private static boolean isRead(FhirOperation operation) {
        return operation == FhirOperation.READ
                || operation == FhirOperation.SEARCH
                || operation == FhirOperation.HISTORY;
    }

    private static boolean isWrite(FhirOperation operation) {
        return operation == FhirOperation.CREATE
                || operation == FhirOperation.UPDATE
                || operation == FhirOperation.DELETE;
    }
}
