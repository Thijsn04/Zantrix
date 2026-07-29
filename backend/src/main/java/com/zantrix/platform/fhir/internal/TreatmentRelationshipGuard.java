package com.zantrix.platform.fhir.internal;

import com.zantrix.platform.security.EmergencyAccessContext;
import jakarta.annotation.PostConstruct;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Requires the acting clinician to have a reason to be in this patient's record.
 *
 * A role check answers what kind of thing a user may do. It does not answer
 * whether this user has anything to do with this patient, so on its own it
 * grants every clinician access to the entire population. This closes that gap
 * at the same boundary as consent, which means no application module can go
 * around it.
 *
 * <p>Three modes, because the right answer differs by deployment:
 *
 * <ul>
 *   <li>{@code off}: no relationship requirement. This is the historical
 *       behaviour and is not acceptable for real clinical use, so the
 *       application says so at startup rather than leaving it unnoticed.</li>
 *   <li>{@code organization}: the patient must be managed by an organization
 *       the clinician works for. Suitable for a single clinic.</li>
 *   <li>{@code care-relationship}: additionally requires evidence of care,
 *       being on the patient's care team or having had an encounter with
 *       them. Stricter, and it needs that evidence to exist before access.</li>
 * </ul>
 *
 * <p>Emergency access bypasses this check exactly as it bypasses consent: it is
 * already justified, prominently audited, and creates a mandatory review.
 */
@Component
class TreatmentRelationshipGuard implements FhirRelationshipPolicy {

    private static final Logger log = LoggerFactory.getLogger(TreatmentRelationshipGuard.class);
    private static final Set<String> MODES = Set.of("off", "organization", "care-relationship");

    /** Resource types that are not patient records and must stay reachable to find one. */
    private static final Set<String> EXEMPT = Set.of("Patient", "Practitioner", "PractitionerRole",
            "Organization", "Location", "Schedule", "Slot", "Consent");

    private final FhirTransport transport;
    private final String mode;

    TreatmentRelationshipGuard(FhirTransport transport,
                               @Value("${zantrix.access.relationship-mode:off}") String mode) {
        this.transport = transport;
        this.mode = mode;
        if (!MODES.contains(mode)) {
            throw new IllegalStateException("Unsupported relationship mode: " + mode);
        }
    }

    @PostConstruct
    void warnWhenDisabled() {
        if ("off".equals(mode)) {
            log.warn("Treatment relationship enforcement is off. Any authenticated clinician can open any "
                    + "patient record. Set zantrix.access.relationship-mode before real clinical use.");
        }
    }

    @Override
    public void authorize(String resourceType, String patientId) {
        if ("off".equals(mode) || patientId == null || patientId.isBlank()
                || EXEMPT.contains(resourceType) || EmergencyAccessContext.active()) {
            return;
        }
        String practitionerId = currentPractitioner();
        if (practitionerId == null) {
            // A token with no practitioner identity cannot have a care relationship.
            throw new AccessDeniedException("No practitioner identity to establish a treatment relationship");
        }
        if (sharesOrganization(practitionerId, patientId)) {
            return;
        }
        if ("care-relationship".equals(mode) && hasCareRelationship(practitionerId, patientId)) {
            return;
        }
        throw new AccessDeniedException(
                "No treatment relationship with this patient. Use emergency access if this is clinically necessary.");
    }

    private static String currentPractitioner() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || !authentication.isAuthenticated() ? null : authentication.getName();
    }

    /** The clinician works for an organization that manages this patient. */
    private boolean sharesOrganization(String practitionerId, String patientId) {
        Set<String> mine = organizationsOf(practitionerId);
        if (mine.isEmpty()) {
            return false;
        }
        Patient patient = read(Patient.class, patientId);
        if (patient == null || !patient.hasManagingOrganization()) {
            return false;
        }
        return mine.contains(patient.getManagingOrganization().getReferenceElement().getIdPart());
    }

    private Set<String> organizationsOf(String practitionerId) {
        return search(PractitionerRole.class, "PractitionerRole", Map.of(
                        "practitioner", List.of("Practitioner/" + practitionerId),
                        "active", List.of("true"), "_count", List.of("50"))).stream()
                .filter(PractitionerRole::hasOrganization)
                .map(role -> role.getOrganization().getReferenceElement().getIdPart())
                .collect(Collectors.toSet());
    }

    /** Evidence that this clinician is actually involved in this patient's care. */
    private boolean hasCareRelationship(String practitionerId, String patientId) {
        boolean onCareTeam = search(CareTeam.class, "CareTeam", Map.of(
                "patient", List.of(patientId), "status", List.of("active"), "_count", List.of("50"))).stream()
                .flatMap(team -> team.getParticipant().stream())
                .map(participant -> participant.getMember().getReferenceElement().getIdPart())
                .anyMatch(practitionerId::equals);
        if (onCareTeam) {
            return true;
        }
        return search(Encounter.class, "Encounter", Map.of(
                "patient", List.of(patientId), "_count", List.of("100"))).stream()
                .flatMap(encounter -> encounter.getParticipant().stream())
                .map(participant -> participant.getIndividual().getReferenceElement().getIdPart())
                .anyMatch(practitionerId::equals);
    }

    private <T extends Resource> T read(Class<T> type, String id) {
        try {
            return transport.read(type, id);
        } catch (RuntimeException notReadable) {
            return null;
        }
    }

    private <T extends Resource> List<T> search(Class<T> type, String resourceType,
                                                Map<String, List<String>> parameters) {
        Bundle bundle = transport.search(resourceType, parameters);
        return bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(type::isInstance).map(type::cast)
                .toList();
    }

}
