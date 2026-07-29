package com.zantrix.patient.internal;

import com.zantrix.patient.PatientRegistration;
import com.zantrix.patient.PatientSummary;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

final class PatientMapper {

    private PatientMapper() {
    }

    static Patient toFhir(PatientRegistration request) {
        Patient patient = new Patient();
        patient.setId(UUID.randomUUID().toString());
        patient.setActive(true);
        patient.addName().setFamily(request.familyName()).addGiven(request.givenName());
        patient.setBirthDate(Date.from(request.birthDate().atStartOfDay().toInstant(ZoneOffset.UTC)));
        patient.setGender(parseGender(request.administrativeGender()));
        if (request.email() != null && !request.email().isBlank()) {
            patient.addTelecom().setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue(request.email());
        }
        if (request.phone() != null && !request.phone().isBlank()) {
            patient.addTelecom().setSystem(ContactPoint.ContactPointSystem.PHONE).setValue(request.phone());
        }
        if (request.identifierSystem() != null && !request.identifierSystem().isBlank()
                && request.identifierValue() != null && !request.identifierValue().isBlank()) {
            patient.addIdentifier(new Identifier().setSystem(request.identifierSystem())
                    .setValue(request.identifierValue()));
        }
        if (request.managingOrganizationId() != null && !request.managingOrganizationId().isBlank()) {
            // Recorded at registration because the treatment relationship policy
            // resolves access from it, and it cannot be inferred afterwards.
            patient.setManagingOrganization(
                    new org.hl7.fhir.r4.model.Reference("Organization/" + request.managingOrganizationId()));
        }
        return patient;
    }

    static PatientSummary summary(Patient patient, double duplicateScore) {
        String displayName = patient.getName().isEmpty() ? "" : patient.getNameFirstRep().getNameAsSingleString();
        LocalDate birthDate = patient.getBirthDate() == null ? null
                : patient.getBirthDate().toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        String identifier = patient.getIdentifier().isEmpty() ? null
                : patient.getIdentifierFirstRep().getValue();
        return new PatientSummary(patient.getIdElement().getIdPart(), displayName, birthDate,
                patient.hasGender() ? patient.getGender().toCode() : null, patient.getActive(),
                identifier, duplicateScore);
    }

    private static Enumerations.AdministrativeGender parseGender(String value) {
        return switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "male" -> Enumerations.AdministrativeGender.MALE;
            case "female" -> Enumerations.AdministrativeGender.FEMALE;
            case "other" -> Enumerations.AdministrativeGender.OTHER;
            case "unknown" -> Enumerations.AdministrativeGender.UNKNOWN;
            default -> throw new IllegalArgumentException("administrativeGender must be male, female, other, or unknown");
        };
    }
}
