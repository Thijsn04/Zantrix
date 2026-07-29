package com.zantrix.coverage.internal;

import com.zantrix.coverage.CoverageRequest;
import com.zantrix.coverage.CoverageSummary;
import com.zantrix.platform.fhir.FhirAccessGateway;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Reference;
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

class CoverageServiceTest {

    private FhirAccessGateway fhir;
    private CoverageService service;

    @BeforeEach
    void setUp() {
        fhir = mock(FhirAccessGateway.class);
        service = new CoverageService(fhir);
    }

    private static CoverageRequest request(String relationship, LocalDate start, LocalDate end) {
        return new CoverageRequest("p-1", "org-9", "Zorgverzekeraar Noord", "EHCPOL",
                "Health insurance", relationship, "POL-88213", "GRP-4", start, end);
    }

    @Test
    void recordsActiveCoverageAgainstThePatient() {
        CoverageSummary summary = service.add(request("self", LocalDate.of(2026, 1, 1), null));

        assertThat(summary.status()).isEqualTo("active");
        assertThat(summary.patientId()).isEqualTo("p-1");
        assertThat(summary.subscriberId()).isEqualTo("POL-88213");
        assertThat(summary.groupNumber()).isEqualTo("GRP-4");
        assertThat(summary.end()).isNull();
        verify(fhir).create(any(Coverage.class), anyString());
    }

    @Test
    void refusesAnUnknownSubscriberRelationship() {
        assertThatThrownBy(() -> service.add(request("landlord", LocalDate.now(), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("relationship");
        verifyNoInteractions(fhir);
    }

    @Test
    void refusesCoverageThatEndsBeforeItStarts() {
        assertThatThrownBy(() -> service.add(
                request("self", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 1, 1))))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(fhir);
    }

    @Test
    void endingCoverageKeepsItInTheRecord() {
        Coverage existing = new Coverage();
        existing.setId("cv-1");
        existing.setStatus(Coverage.CoverageStatus.ACTIVE);
        existing.setBeneficiary(new Reference("Patient/p-1"));
        when(fhir.read(Coverage.class, "cv-1", "p-1")).thenReturn(existing);

        CoverageSummary summary = service.cancel("cv-1", "p-1");

        assertThat(summary.status()).isEqualTo("cancelled");
        // Ended, not deleted: what applied at the time of care has to stay readable.
        assertThat(summary.end()).isNotNull();
        verify(fhir).update(any(Coverage.class), anyString());
    }

    @Test
    void refusesToEndCoverageTwice() {
        Coverage existing = new Coverage();
        existing.setId("cv-1");
        existing.setStatus(Coverage.CoverageStatus.CANCELLED);
        when(fhir.read(Coverage.class, "cv-1", "p-1")).thenReturn(existing);

        assertThatThrownBy(() -> service.cancel("cv-1", "p-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
