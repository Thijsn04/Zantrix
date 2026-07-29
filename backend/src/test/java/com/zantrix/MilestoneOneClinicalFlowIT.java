package com.zantrix;

import com.zantrix.allergies.AllergyRequest;
import com.zantrix.allergies.internal.AllergyService;
import com.zantrix.cds.MedicationCodeValidator;
import com.zantrix.documentation.NoteRequest;
import com.zantrix.documentation.NoteSection;
import com.zantrix.documentation.internal.DocumentationService;
import com.zantrix.encounter.EncounterRequest;
import com.zantrix.encounter.internal.EncounterService;
import com.zantrix.medications.PrescriptionRequest;
import com.zantrix.medications.internal.MedicationService;
import com.zantrix.orders.OrderRequest;
import com.zantrix.orders.ResultMeasurement;
import com.zantrix.orders.ResultRequest;
import com.zantrix.orders.internal.OrderService;
import com.zantrix.patient.PatientRegistration;
import com.zantrix.patient.internal.PatientService;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.problems.ProblemRequest;
import com.zantrix.problems.internal.ProblemService;
import com.zantrix.scheduling.AppointmentRequest;
import com.zantrix.scheduling.internal.SchedulingService;
import com.zantrix.terminology.TerminologyValidator;
import com.zantrix.vitals.VitalMeasurement;
import com.zantrix.vitals.VitalsRequest;
import com.zantrix.vitals.internal.VitalsService;
import org.junit.jupiter.api.Test;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Exercises the complete Milestone 1 clinician path against a real R4 server. */
@Testcontainers
@Import(MilestoneOneClinicalFlowIT.PermissiveTerminology.class)
class MilestoneOneClinicalFlowIT extends IntegrationTestBase {
    @Container
    static final GenericContainer<?> HAPI = new GenericContainer<>("hapiproject/hapi:v8.10.0-2")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/fhir/metadata").forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(5)));

    @DynamicPropertySource
    static void fhirProperties(DynamicPropertyRegistry registry) {
        registry.add("zantrix.fhir.base-url",
                () -> "http://" + HAPI.getHost() + ":" + HAPI.getMappedPort(8080) + "/fhir");
    }

    @MockBean MedicationCodeValidator medicationCodes;
    @Autowired PatientService patients;
    @Autowired SchedulingService scheduling;
    @Autowired EncounterService encounters;
    @Autowired ProblemService problems;
    @Autowired AllergyService allergies;
    @Autowired VitalsService vitals;
    @Autowired OrderService orders;
    @Autowired MedicationService medications;
    @Autowired DocumentationService notes;
    @Autowired FhirAccessGateway fhir;

    @TestConfiguration
    static class PermissiveTerminology {
        @Bean
        @Primary
        TerminologyValidator terminologyValidator() {
            return (system, code, display) -> { };
        }
    }

    @Test
    @WithMockUser(username = "clinician", authorities = {"ROLE_PHYSICIAN", "SCOPE_user/*.cruds"})
    void completesTheOutpatientCoreWithoutFabricatedData() {
        String patientId = patients.register(new PatientRegistration("Avery", "Morgan",
                LocalDate.of(1991, 4, 12), "female", "avery@example.test", null,
                "urn:zantrix:test:mrn", "M1-001", null, false)).id();
        String practitionerId = "m1-practitioner";
        Practitioner practitioner = new Practitioner();
        practitioner.setId(practitionerId);
        practitioner.setActive(true);
        practitioner.addName().setFamily("Clinician").addGiven("M1");
        fhir.create(practitioner, null);
        Instant start = Instant.now().plus(Duration.ofHours(1));
        assertThat(scheduling.book(new AppointmentRequest(patientId, practitionerId, null, null,
                start, start.plus(Duration.ofMinutes(30)), "185349003", "Outpatient consultation", null)).status())
                .isEqualTo("booked");

        String encounterId = encounters.create(new EncounterRequest(patientId, practitionerId, null, null,
                "185349003", "Outpatient consultation", start)).id();
        assertThat(encounters.start(encounterId, patientId).status()).isEqualTo("in-progress");
        assertThat(problems.add(new ProblemRequest(patientId, encounterId, "http://snomed.info/sct",
                "44054006", "Diabetes mellitus type 2", LocalDate.now(), null)).clinicalStatus()).isEqualTo("active");
        assertThat(allergies.add(new AllergyRequest(patientId, encounterId, "http://snomed.info/sct",
                "387517004", "Paracetamol", "medication", "unable-to-assess", null, null,
                null, null, "Patient reported")).clinicalStatus()).isEqualTo("active");
        assertThat(vitals.record(new VitalsRequest(patientId, encounterId, practitionerId, Instant.now(), List.of(
                new VitalMeasurement("29463-7", new BigDecimal("70"), "kg", null),
                new VitalMeasurement("8302-2", new BigDecimal("175"), "cm", null)))))
                .extracting("loincCode").contains("39156-5");

        var order = orders.place(new OrderRequest(patientId, encounterId, practitionerId, "laboratory",
                "http://loinc.org", "4548-4", "Hemoglobin A1c", "routine", null));
        assertThat(orders.result(order.id(), new ResultRequest(patientId, practitionerId, Instant.now(),
                List.of(new ResultMeasurement("http://loinc.org", "4548-4", "Hemoglobin A1c",
                        new BigDecimal("6.7"), "%", null, new BigDecimal("4"),
                        new BigDecimal("6"), "H")), "Above reference range")).status()).isEqualTo("final");

        assertThat(medications.prescribe(new PrescriptionRequest(patientId, encounterId, practitionerId,
                "6809", "Metformin", "500 mg twice daily", null, null, null,
                new BigDecimal("500"), "mg", 2, 30, new BigDecimal("60"), 0,
                "Diabetes", null)).status()).isEqualTo("active");

        var note = notes.draft(new NoteRequest(patientId, encounterId, practitionerId,
                "http://loinc.org", "34117-2", "History and physical note", "Consultation",
                List.of(new NoteSection("http://loinc.org", "51847-2", "Assessment and plan",
                        "Continue treatment and review results."))));
        assertThat(notes.sign(note.id(), patientId, practitionerId).status()).isEqualTo("final");
        assertThat(encounters.finish(encounterId, patientId).status()).isEqualTo("finished");
    }
}
