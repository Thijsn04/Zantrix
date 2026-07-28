package com.zantrix.immunizations.internal;

import com.zantrix.immunizations.ImmunizationRequest;
import com.zantrix.immunizations.ImmunizationSummary;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.terminology.TerminologyValidator;
import org.hl7.fhir.r4.model.Immunization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ImmunizationServiceTest {

    private FhirAccessGateway fhir;
    private ImmunizationService service;

    @BeforeEach
    void setUp() {
        fhir = mock(FhirAccessGateway.class);
        service = new ImmunizationService(fhir, mock(TerminologyValidator.class));
    }

    private static ImmunizationRequest request(LocalDate occurrence, Integer dose) {
        return new ImmunizationRequest("p-1", "e-1", "pr-1", "http://snomed.info/sct",
                "871875004", "Influenza vaccine", occurrence, "FL-2291", "Left deltoid",
                "Intramuscular", dose, null);
    }

    @Test
    void recordsACompletedDoseAgainstThePatient() {
        ImmunizationSummary summary = service.record(request(LocalDate.now().minusDays(1), 2));

        assertThat(summary.status()).isEqualTo("completed");
        assertThat(summary.doseNumber()).isEqualTo(2);
        assertThat(summary.patientId()).isEqualTo("p-1");
        verify(fhir).create(any(Immunization.class), anyString());
    }

    @Test
    void refusesAVaccinationDatedInTheFuture() {
        assertThatThrownBy(() -> service.record(request(LocalDate.now().plusDays(1), 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
        verifyNoInteractions(fhir);
    }

    @Test
    void refusesANonPositiveDoseNumber() {
        assertThatThrownBy(() -> service.record(request(LocalDate.now(), 0)))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(fhir);
    }

    @Test
    void correctingAnEntryRequiresAReason() {
        assertThatThrownBy(() -> service.markEnteredInError("im-1", "p-1", "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reason");
        verifyNoInteractions(fhir);
    }

    @Test
    void correctingKeepsTheDoseInTheRecordRatherThanRemovingIt() {
        Immunization existing = new Immunization();
        existing.setId("im-1");
        existing.setStatus(Immunization.ImmunizationStatus.COMPLETED);
        existing.setPatient(new org.hl7.fhir.r4.model.Reference("Patient/p-1"));
        when(fhir.read(Immunization.class, "im-1", "p-1")).thenReturn(existing);

        ImmunizationSummary summary = service.markEnteredInError("im-1", "p-1", "Wrong patient");

        assertThat(summary.status()).isEqualTo("entered-in-error");
        assertThat(summary.statusReason()).isEqualTo("Wrong patient");
        // Updated, never deleted: a record that quietly loses a dose is the greater hazard.
        verify(fhir).update(any(Immunization.class), anyString());
    }

    @Test
    void refusesToCorrectAnEntryTwice() {
        Immunization existing = new Immunization();
        existing.setId("im-1");
        existing.setStatus(Immunization.ImmunizationStatus.ENTEREDINERROR);
        when(fhir.read(Immunization.class, "im-1", "p-1")).thenReturn(existing);

        assertThatThrownBy(() -> service.markEnteredInError("im-1", "p-1", "Again"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
