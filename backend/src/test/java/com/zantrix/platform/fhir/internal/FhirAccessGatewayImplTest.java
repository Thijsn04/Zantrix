package com.zantrix.platform.fhir.internal;

import ca.uhn.fhir.rest.api.MethodOutcome;
import com.zantrix.audit.AuditAction;
import com.zantrix.audit.AuditEntry;
import com.zantrix.audit.AuditOutcome;
import com.zantrix.audit.AuditRecorder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FhirAccessGatewayImplTest {

    private final FakeTransport transport = new FakeTransport();
    private final FakeAccessPolicy policy = new FakeAccessPolicy();
    private final RecordingAudit audit = new RecordingAudit();
    private final FhirAccessGatewayImpl gateway = new FhirAccessGatewayImpl(
            transport,
            policy,
            (resourceType, patientId) -> { }, (resourceType, patientId) -> { },
            audit,
            new FakeMutationJournal(),
            new FakeEmergencyReviews());

    @Test
    void authorizedReadUsesTheTransportAndRecordsSuccess() {
        Patient patient = new Patient();
        patient.setId("p1");
        transport.patient = patient;

        Patient result = gateway.read(Patient.class, "p1", "p1");

        assertThat(result).isSameAs(patient);
        assertThat(policy.lastOperation).isEqualTo(FhirOperation.READ);
        assertThat(transport.readCalls).isEqualTo(1);
        assertThat(audit.lastEntry).isEqualTo(
                new AuditEntry(AuditAction.READ, "Patient", "p1", "p1", AuditOutcome.SUCCESS, false));
    }

    @Test
    void deniedReadNeverReachesHapiAndRecordsTheFailedAttempt() {
        policy.deny = true;

        assertThatThrownBy(() -> gateway.read(Patient.class, "p1", "p1"))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(transport.readCalls).isZero();
        assertThat(audit.lastEntry.outcome()).isEqualTo(AuditOutcome.FAILURE);
    }

    @Test
    void hapiFailureIsAuditedAndPropagated() {
        transport.failure = new IllegalStateException("HAPI unavailable");

        assertThatThrownBy(() -> gateway.read(Patient.class, "p1", "p1"))
                .isSameAs(transport.failure);

        assertThat(transport.readCalls).isEqualTo(1);
        assertThat(audit.lastEntry.outcome()).isEqualTo(AuditOutcome.FAILURE);
    }

    private static final class FakeAccessPolicy implements FhirAccessPolicy {
        private boolean deny;
        private FhirOperation lastOperation;

        @Override
        public void authorize(FhirOperation operation, String resourceType, String resourceId,
                              String patientId) {
            lastOperation = operation;
            if (deny) {
                throw new AccessDeniedException("denied");
            }
        }
    }

    private static final class RecordingAudit implements AuditRecorder {
        private AuditEntry lastEntry;

        @Override
        public void record(AuditEntry entry) {
            lastEntry = entry;
        }
    }

    private static final class FakeTransport implements FhirTransport {
        private Patient patient;
        private int readCalls;
        private RuntimeException failure;

        @Override
        public CapabilityStatement capabilities() {
            return new CapabilityStatement();
        }

        @Override
        public <T extends IBaseResource> T read(Class<T> resourceType, String id) {
            readCalls++;
            if (failure != null) {
                throw failure;
            }
            return resourceType.cast(patient);
        }

        @Override
        public Bundle search(String resourceType, java.util.Map<String, java.util.List<String>> parameters) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle searchAll(String resourceType, java.util.Map<String, java.util.List<String>> parameters) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle history(String resourceType, String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle transaction(Bundle bundle) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MethodOutcome create(IBaseResource resource) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MethodOutcome update(IBaseResource resource) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MethodOutcome delete(String resourceType, String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String resourceName(Class<? extends IBaseResource> resourceType) {
            return "Patient";
        }
    }

    private static final class FakeMutationJournal implements FhirMutationJournal {
        @Override public java.util.UUID begin(FhirOperation operation, String resourceType,
                                              String resourceId, String patientId) {
            return java.util.UUID.randomUUID();
        }
        @Override public void remoteSucceeded(java.util.UUID id, String resolvedResourceId) { }
        @Override public void audited(java.util.UUID id) { }
        @Override public void remoteFailed(java.util.UUID id, Throwable failure) { }
    }

    private static final class FakeEmergencyReviews
            implements com.zantrix.platform.security.EmergencyAccessReviewRecorder {
        @Override public void record(String resourceType, String resourceId, String patientId) { }
        @Override public java.util.List<com.zantrix.platform.security.EmergencyAccessReview> openReviews() {
            return java.util.List.of();
        }
        @Override public com.zantrix.platform.security.EmergencyAccessReview review(
                java.util.UUID id, String outcomeCode) {
            throw new UnsupportedOperationException();
        }
    }
}
