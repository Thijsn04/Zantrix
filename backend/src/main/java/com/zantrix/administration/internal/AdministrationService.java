package com.zantrix.administration.internal;

import com.zantrix.administration.DirectorySummary;
import com.zantrix.administration.FeatureFlagView;
import com.zantrix.administration.LocationRequest;
import com.zantrix.administration.OrganizationRequest;
import com.zantrix.administration.PractitionerRequest;
import com.zantrix.administration.PractitionerSummary;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdministrationService {
    private static final String IDENTITY_SUBJECT = "https://zantrix.org/identifier/identity-subject";
    private final FhirAccessGateway fhir;
    private final FeatureFlagRepository flags;
    public AdministrationService(FhirAccessGateway fhir, FeatureFlagRepository flags) {
        this.fhir=fhir; this.flags=flags;
    }

    public DirectorySummary createOrganization(OrganizationRequest request) {
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID().toString());
        organization.setActive(true);
        organization.setName(request.name());
        if (request.identifierSystem()!=null && !request.identifierSystem().isBlank()
                && request.identifierValue()!=null && !request.identifierValue().isBlank()) {
            organization.addIdentifier(new Identifier().setSystem(request.identifierSystem())
                    .setValue(request.identifierValue()));
        }
        fhir.create(organization, null);
        return summary(organization);
    }

    public DirectorySummary createLocation(LocationRequest request) {
        Location location = new Location();
        location.setId(UUID.randomUUID().toString());
        location.setStatus(Location.LocationStatus.ACTIVE);
        location.setName(request.name());
        location.addType(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/v3-RoleCode",
                request.typeCode(), request.typeDisplay())));
        location.setManagingOrganization(new Reference("Organization/"+request.organizationId()));
        if (request.addressLine()!=null) location.getAddress().addLine(request.addressLine());
        location.getAddress().setCity(request.city()).setPostalCode(request.postalCode()).setCountry(request.country());
        fhir.create(location, null);
        return summary(location);
    }

    public List<DirectorySummary> organizations() {
        return FhirBundles.resources(fhir.search("Organization", Map.of("active",List.of("true"),"_count",List.of("300")),null), Organization.class)
                .stream().map(AdministrationService::summary).toList();
    }
    public List<DirectorySummary> locations(String organizationId) {
        return FhirBundles.resources(fhir.search("Location", Map.of("organization",List.of(organizationId),"status",List.of("active"),"_count",List.of("300")),null), Location.class)
                .stream().map(AdministrationService::summary).toList();
    }

    public PractitionerSummary createPractitioner(PractitionerRequest request) {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(UUID.randomUUID().toString());
        practitioner.setActive(true);
        practitioner.addIdentifier().setSystem(IDENTITY_SUBJECT).setValue(request.identitySubject());
        org.hl7.fhir.r4.model.HumanName name = practitioner.addName().setFamily(request.familyName());
        if (request.givenName() != null && !request.givenName().isBlank()) {
            name.addGiven(request.givenName());
        }

        PractitionerRole role = new PractitionerRole();
        role.setId(UUID.randomUUID().toString());
        role.setActive(true);
        role.setPractitioner(new Reference("Practitioner/" + practitioner.getIdElement().getIdPart()));
        role.setOrganization(new Reference("Organization/" + request.organizationId()));
        role.addCode(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/practitioner-role",
                request.roleCode(), request.roleDisplay())));
        if (request.locationId() != null && !request.locationId().isBlank()) {
            role.addLocation(new Reference("Location/" + request.locationId()));
        }
        org.hl7.fhir.r4.model.Bundle bundle = new org.hl7.fhir.r4.model.Bundle()
                .setType(org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION);
        bundle.addEntry().setResource(practitioner).getRequest()
                .setMethod(org.hl7.fhir.r4.model.Bundle.HTTPVerb.PUT)
                .setUrl("Practitioner/" + practitioner.getIdElement().getIdPart());
        bundle.addEntry().setResource(role).getRequest()
                .setMethod(org.hl7.fhir.r4.model.Bundle.HTTPVerb.PUT)
                .setUrl("PractitionerRole/" + role.getIdElement().getIdPart());
        fhir.transaction(bundle, null);
        return summary(practitioner, role);
    }

    public List<PractitionerSummary> practitioners() {
        List<Practitioner> practitioners = FhirBundles.resources(fhir.search("Practitioner",
                Map.of("active", List.of("true"), "_count", List.of("300")), null), Practitioner.class);
        return practitioners.stream().map(practitioner -> {
            List<PractitionerRole> roles = FhirBundles.resources(fhir.search("PractitionerRole",
                    Map.of("practitioner", List.of(practitioner.getIdElement().getIdPart()),
                            "active", List.of("true"), "_count", List.of("20")), null), PractitionerRole.class);
            return summary(practitioner, roles.isEmpty() ? null : roles.getFirst());
        }).toList();
    }

    public List<FeatureFlagView> flags() {
        return flags.findAll().stream().map(value->new FeatureFlagView(value.getName(),value.isEnabled(),value.getUpdatedAt())).toList();
    }
    @Transactional
    public FeatureFlagView setFlag(String name, boolean enabled) {
        if (!name.matches("[a-z0-9-]{2,128}")) throw new IllegalArgumentException("Invalid feature flag name");
        String actor=SecurityContextHolder.getContext().getAuthentication().getName();
        FeatureFlagEntity flag=flags.findById(name).orElseGet(()->new FeatureFlagEntity(name,enabled, Instant.now(),actor));
        flag.update(enabled,actor); flags.save(flag);
        return new FeatureFlagView(flag.getName(),flag.isEnabled(),flag.getUpdatedAt());
    }

    private static DirectorySummary summary(Organization value) {
        return new DirectorySummary(value.getIdElement().getIdPart(),"Organization",value.getName(),value.getActive(),null);
    }
    private static DirectorySummary summary(Location value) {
        return new DirectorySummary(value.getIdElement().getIdPart(),"Location",value.getName(),value.getStatus()==Location.LocationStatus.ACTIVE,
                value.hasManagingOrganization()?value.getManagingOrganization().getReferenceElement().getIdPart():null);
    }

    private static PractitionerSummary summary(Practitioner practitioner, PractitionerRole role) {
        String subject = practitioner.getIdentifier().stream()
                .filter(identifier -> IDENTITY_SUBJECT.equals(identifier.getSystem()))
                .map(Identifier::getValue).findFirst().orElse(null);
        Coding code = role == null || role.getCode().isEmpty() ? new Coding()
                : role.getCodeFirstRep().getCodingFirstRep();
        return new PractitionerSummary(practitioner.getIdElement().getIdPart(), subject,
                practitioner.getNameFirstRep().getNameAsSingleString(), code.getCode(), code.getDisplay(),
                role == null ? null : role.getOrganization().getReferenceElement().getIdPart(),
                role == null || role.getLocation().isEmpty() ? null
                        : role.getLocationFirstRep().getReferenceElement().getIdPart(), practitioner.getActive());
    }
}
