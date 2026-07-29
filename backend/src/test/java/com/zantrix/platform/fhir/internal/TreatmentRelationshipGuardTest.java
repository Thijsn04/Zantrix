package com.zantrix.platform.fhir.internal;

import com.zantrix.platform.security.EmergencyAccessContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TreatmentRelationshipGuardTest {

    private final Map<String, List<Resource>> searches = new LinkedHashMap<>();
    private final Map<String, Resource> reads = new LinkedHashMap<>();

    /** Only search and read are used by the guard; the rest is never reached. */
    private final FhirTransport transport = new FhirTransport() {
        @Override public org.hl7.fhir.r4.model.CapabilityStatement capabilities() { throw new UnsupportedOperationException(); }
        @Override public <T extends IBaseResource> T read(Class<T> type, String id) {
            Resource found = reads.get(id);
            if (found == null) throw new IllegalStateException("not found");
            return type.cast(found);
        }
        @Override public Bundle search(String resourceType, Map<String, List<String>> parameters) {
            Bundle bundle = new Bundle();
            searches.getOrDefault(resourceType, List.of())
                    .forEach(resource -> bundle.addEntry().setResource(resource));
            return bundle;
        }
        @Override public Bundle searchAll(String resourceType, Map<String, List<String>> parameters) {
            return search(resourceType, parameters);
        }
        @Override public Bundle history(String resourceType, String id) { throw new UnsupportedOperationException(); }
        @Override public Bundle transaction(Bundle bundle) { throw new UnsupportedOperationException(); }
        @Override public ca.uhn.fhir.rest.api.MethodOutcome create(IBaseResource resource) { throw new UnsupportedOperationException(); }
        @Override public ca.uhn.fhir.rest.api.MethodOutcome update(IBaseResource resource) { throw new UnsupportedOperationException(); }
        @Override public ca.uhn.fhir.rest.api.MethodOutcome delete(String resourceType, String id) { throw new UnsupportedOperationException(); }
        @Override public String resourceName(Class<? extends IBaseResource> type) { return type.getSimpleName(); }
    };

    @BeforeEach
    void signIn() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("pr-9", "n/a", List.of()));
    }

    @AfterEach
    void signOut() {
        SecurityContextHolder.clearContext();
        EmergencyAccessContext.clear();
    }

    private void practitionerWorksFor(String organizationId) {
        PractitionerRole role = new PractitionerRole();
        role.setPractitioner(new Reference("Practitioner/pr-9"));
        role.setOrganization(new Reference("Organization/" + organizationId));
        searches.computeIfAbsent("PractitionerRole", key -> new ArrayList<>()).add(role);
    }

    private void patientManagedBy(String organizationId) {
        Patient patient = new Patient();
        patient.setId("p-1");
        if (organizationId != null) {
            patient.setManagingOrganization(new Reference("Organization/" + organizationId));
        }
        reads.put("p-1", patient);
    }

    private TreatmentRelationshipGuard guard(String mode) {
        return new TreatmentRelationshipGuard(transport, mode);
    }

    @Test
    void offPermitsEverythingSoExistingDeploymentsAreNotBrokenSilently() {
        patientManagedBy("org-other");
        assertThatCode(() -> guard("off").authorize("Observation", "p-1")).doesNotThrowAnyException();
    }

    @Test
    void organizationModePermitsAClinicianOfTheManagingOrganization() {
        practitionerWorksFor("org-1");
        patientManagedBy("org-1");
        assertThatCode(() -> guard("organization").authorize("Observation", "p-1")).doesNotThrowAnyException();
    }

    @Test
    void organizationModeRefusesAClinicianFromAnotherOrganization() {
        practitionerWorksFor("org-1");
        patientManagedBy("org-2");
        assertThatThrownBy(() -> guard("organization").authorize("Observation", "p-1"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("No treatment relationship");
    }

    @Test
    void aPatientWithNoManagingOrganizationIsNotReachable() {
        practitionerWorksFor("org-1");
        patientManagedBy(null);
        // Fails closed. Registration records the organization precisely so this does not happen.
        assertThatThrownBy(() -> guard("organization").authorize("Observation", "p-1"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void careRelationshipModeAcceptsMembershipOfThePatientsCareTeam() {
        practitionerWorksFor("org-1");
        patientManagedBy("org-2");
        CareTeam team = new CareTeam();
        team.addParticipant().setMember(new Reference("Practitioner/pr-9"));
        searches.put("CareTeam", List.of(team));

        assertThatCode(() -> guard("care-relationship").authorize("Observation", "p-1"))
                .doesNotThrowAnyException();
    }

    @Test
    void careRelationshipModeAcceptsAnEncounterWithThePatient() {
        practitionerWorksFor("org-1");
        patientManagedBy("org-2");
        Encounter encounter = new Encounter();
        encounter.addParticipant().setIndividual(new Reference("Practitioner/pr-9"));
        searches.put("Encounter", List.of(encounter));

        assertThatCode(() -> guard("care-relationship").authorize("Observation", "p-1"))
                .doesNotThrowAnyException();
    }

    @Test
    void careRelationshipModeRefusesAClinicianWithNoInvolvement() {
        practitionerWorksFor("org-1");
        patientManagedBy("org-2");
        assertThatThrownBy(() -> guard("care-relationship").authorize("Observation", "p-1"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void findingAPatientStaysPossibleOrNobodyCouldEverStart() {
        practitionerWorksFor("org-1");
        patientManagedBy("org-2");
        // Patient and the directory are exempt: they are how a clinician locates
        // the record they then need a relationship for.
        assertThatCode(() -> guard("care-relationship").authorize("Patient", "p-1")).doesNotThrowAnyException();
        assertThatCode(() -> guard("care-relationship").authorize("Organization", "p-1")).doesNotThrowAnyException();
    }

    @Test
    void emergencyAccessBypassesTheCheckAsItDoesForConsent() {
        practitionerWorksFor("org-1");
        patientManagedBy("org-2");
        EmergencyAccessContext.open("immediate-threat");

        assertThatCode(() -> guard("care-relationship").authorize("Observation", "p-1"))
                .doesNotThrowAnyException();
    }

    @Test
    void anUnknownModeIsRefusedAtStartupRatherThanIgnored() {
        assertThatThrownBy(() -> guard("permissive"))
                .isInstanceOf(IllegalStateException.class);
    }
}
