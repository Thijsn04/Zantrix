package com.zantrix.patient.internal;

import com.zantrix.patient.PatientRegistration;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Patient;

import java.text.Normalizer;
import java.time.ZoneOffset;
import java.util.Locale;

final class DuplicateScorer {

    double score(PatientRegistration candidate, Patient existing) {
        double score = 0;
        if (sameIdentifier(candidate, existing)) {
            return 1;
        }
        if (existing.getBirthDate() != null && candidate.birthDate().equals(existing.getBirthDate()
                .toInstant().atZone(ZoneOffset.UTC).toLocalDate())) {
            score += 0.35;
        }
        if (!existing.getName().isEmpty()) {
            if (normalize(candidate.familyName()).equals(normalize(existing.getNameFirstRep().getFamily()))) {
                score += 0.30;
            }
            if (!existing.getNameFirstRep().getGiven().isEmpty()
                    && normalize(candidate.givenName()).equals(normalize(
                    existing.getNameFirstRep().getGiven().getFirst().getValue()))) {
                score += 0.20;
            }
        }
        if (matchesTelecom(candidate.email(), ContactPoint.ContactPointSystem.EMAIL, existing)
                || matchesTelecom(candidate.phone(), ContactPoint.ContactPointSystem.PHONE, existing)) {
            score += 0.15;
        }
        return Math.min(1, score);
    }

    private static boolean sameIdentifier(PatientRegistration candidate, Patient existing) {
        if (candidate.identifierSystem() == null || candidate.identifierValue() == null) {
            return false;
        }
        return existing.getIdentifier().stream().anyMatch(identifier ->
                candidate.identifierSystem().equals(identifier.getSystem())
                        && candidate.identifierValue().equals(identifier.getValue()));
    }

    private static boolean matchesTelecom(String value, ContactPoint.ContactPointSystem system, Patient existing) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return existing.getTelecom().stream().anyMatch(point -> point.getSystem() == system
                && normalize(value).equals(normalize(point.getValue())));
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\p{Alnum}]", "")
                .toLowerCase(Locale.ROOT);
    }
}
