package com.zantrix.platform.fhir.internal;

import com.zantrix.platform.security.EmergencyAccessContext;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Period;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
class ConsentGuard implements FhirConsentPolicy {

    private final FhirTransport transport;
    private final String mode;

    ConsentGuard(FhirTransport transport,
                 @Value("${zantrix.privacy.consent-mode:deny-on-explicit}") String mode) {
        this.transport = transport;
        this.mode = mode;
    }

    @Override
    public void authorize(String resourceType, String patientId) {
        if (patientId == null || patientId.isBlank() || "Consent".equals(resourceType)
                || EmergencyAccessContext.active()) {
            return;
        }
        Bundle bundle = transport.search("Consent", Map.of("patient", List.of(patientId),
                "status", List.of("active"), "_count", List.of("100")));
        List<Consent> active = bundle.getEntry().stream().map(Bundle.BundleEntryComponent::getResource)
                .filter(Consent.class::isInstance).map(Consent.class::cast)
                .filter(ConsentGuard::currentlyEffective).toList();
        boolean denied = active.stream().anyMatch(consent -> provisionMatches(
                consent.getProvision(), Consent.ConsentProvisionType.DENY, resourceType));
        if (denied) {
            throw new AccessDeniedException("Active patient consent denies this resource access");
        }
        if ("require-active".equals(mode)) {
            boolean permitted = active.stream().anyMatch(consent -> provisionMatches(
                    consent.getProvision(), Consent.ConsentProvisionType.PERMIT, resourceType));
            if (!permitted) {
                throw new AccessDeniedException("No active patient consent permits this resource access");
            }
        } else if (!"deny-on-explicit".equals(mode)) {
            throw new IllegalStateException("Unsupported consent mode: " + mode);
        }
    }

    private static boolean currentlyEffective(Consent consent) {
        Period period = consent.getProvision().getPeriod();
        Date now = new Date();
        return (period.getStart() == null || !period.getStart().after(now))
                && (period.getEnd() == null || !period.getEnd().before(now));
    }

    private static boolean provisionMatches(Consent.ProvisionComponent provision,
                                            Consent.ConsentProvisionType type, String resourceType) {
        if (provision.getType() == type && (provision.getClass_().isEmpty()
                || provision.getClass_().stream().map(Coding::getCode)
                .anyMatch(resourceType::equals))) {
            return true;
        }
        return provision.getProvision().stream().anyMatch(child -> provisionMatches(child, type, resourceType));
    }

}
