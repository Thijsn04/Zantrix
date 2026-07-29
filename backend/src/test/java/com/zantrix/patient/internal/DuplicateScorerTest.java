package com.zantrix.patient.internal;

import com.zantrix.patient.PatientRegistration;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicateScorerTest {
    private final DuplicateScorer scorer = new DuplicateScorer();

    @Test
    void exactIdentifierIsDefinitive() {
        Patient existing = patient();
        existing.addIdentifier().setSystem("urn:mrn").setValue("123");
        assertThat(scorer.score(candidate("123"), existing)).isEqualTo(1);
    }

    @Test
    void normalizedDemographicsAndTelecomReachReviewThreshold() {
        Patient existing = patient();
        assertThat(scorer.score(candidate(null), existing)).isCloseTo(1, org.assertj.core.data.Offset.offset(0.000001));
    }

    @Test
    void unrelatedPatientDoesNotMatch() {
        Patient existing = new Patient();
        existing.addName().setFamily("Different").addGiven("Person");
        existing.setBirthDate(Date.from(LocalDate.of(1980, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertThat(scorer.score(candidate(null), existing)).isZero();
    }

    private static Patient patient() {
        Patient patient = new Patient();
        patient.addName().setFamily("Díaz-Smith").addGiven("Anne Marie");
        patient.setBirthDate(Date.from(LocalDate.of(2000, 2, 3).atStartOfDay().toInstant(ZoneOffset.UTC)));
        patient.addTelecom().setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue("Anne@example.org");
        return patient;
    }

    private static PatientRegistration candidate(String identifier) {
        return new PatientRegistration("Anne-Marie", "Diaz Smith", LocalDate.of(2000, 2, 3),
                "female", "anne@example.org", null, identifier == null ? null : "urn:mrn", identifier, null, false);
    }
}
