package com.zantrix.platform.fhir.internal;

import ca.uhn.fhir.rest.api.MethodOutcome;
import com.zantrix.audit.AuditAction;
import com.zantrix.audit.AuditEntry;
import com.zantrix.audit.AuditOutcome;
import com.zantrix.audit.AuditRecorder;
import com.zantrix.platform.fhir.FhirAccessGateway;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/** Central implementation of authorized and audited FHIR access. */
@Service
class FhirAccessGatewayImpl implements FhirAccessGateway {

    private final FhirTransport transport;
    private final FhirAccessPolicy accessPolicy;
    private final AuditRecorder auditRecorder;

    FhirAccessGatewayImpl(FhirTransport transport, FhirAccessPolicy accessPolicy,
                          AuditRecorder auditRecorder) {
        this.transport = transport;
        this.accessPolicy = accessPolicy;
        this.auditRecorder = auditRecorder;
    }

    @Override
    public CapabilityStatement capabilities() {
        return transport.capabilities();
    }

    @Override
    public <T extends IBaseResource> T read(Class<T> resourceType, String id, String patientId) {
        Objects.requireNonNull(resourceType, "resourceType");
        String type = resourceName(resourceType);
        return execute(FhirOperation.READ, AuditAction.READ, type, required(id, "id"), patientId,
                () -> transport.read(resourceType, id), ignored -> id);
    }

    @Override
    public MethodOutcome create(IBaseResource resource, String patientId) {
        Objects.requireNonNull(resource, "resource");
        String type = resource.fhirType();
        String requestedId = resource.getIdElement().getIdPart();
        return execute(FhirOperation.CREATE, AuditAction.CREATE, type, requestedId, patientId,
                () -> transport.create(resource), outcome -> outcome.getId() == null
                        ? requestedId
                        : outcome.getId().getIdPart());
    }

    @Override
    public MethodOutcome update(IBaseResource resource, String patientId) {
        Objects.requireNonNull(resource, "resource");
        String id = required(resource.getIdElement().getIdPart(), "resource id");
        return execute(FhirOperation.UPDATE, AuditAction.UPDATE, resource.fhirType(), id, patientId,
                () -> transport.update(resource), ignored -> id);
    }

    @Override
    public MethodOutcome delete(String resourceType, String id, String patientId) {
        return execute(FhirOperation.DELETE, AuditAction.DELETE, required(resourceType, "resourceType"),
                required(id, "id"), patientId, () -> transport.delete(resourceType, id), ignored -> id);
    }

    private <T> T execute(FhirOperation operation, AuditAction auditAction, String resourceType,
                          String resourceId, String patientId, Supplier<T> request,
                          Function<T, String> auditedId) {
        T result;
        try {
            accessPolicy.authorize(operation, resourceType, resourceId, patientId);
            result = request.get();
        } catch (RuntimeException failure) {
            recordFailure(auditAction, resourceType, resourceId, patientId, failure);
            throw failure;
        }
        auditRecorder.record(AuditEntry.success(auditAction, resourceType, auditedId.apply(result), patientId));
        return result;
    }

    private void recordFailure(AuditAction action, String resourceType, String resourceId,
                               String patientId, RuntimeException originalFailure) {
        try {
            auditRecorder.record(new AuditEntry(action, resourceType, resourceId, patientId,
                    AuditOutcome.FAILURE, false));
        } catch (RuntimeException auditFailure) {
            originalFailure.addSuppressed(auditFailure);
        }
    }

    private String resourceName(Class<? extends IBaseResource> resourceType) {
        return transport.resourceName(resourceType);
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }
}
