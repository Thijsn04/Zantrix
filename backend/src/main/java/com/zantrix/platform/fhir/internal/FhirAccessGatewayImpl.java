package com.zantrix.platform.fhir.internal;

import ca.uhn.fhir.rest.api.MethodOutcome;
import com.zantrix.audit.AuditAction;
import com.zantrix.audit.AuditEntry;
import com.zantrix.audit.AuditOutcome;
import com.zantrix.audit.AuditRecorder;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirAuditPendingException;
import com.zantrix.platform.security.EmergencyAccessContext;
import com.zantrix.platform.security.EmergencyAccessReviewRecorder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/** Central implementation of authorized and audited FHIR access. */
@Service
class FhirAccessGatewayImpl implements FhirAccessGateway {

    private final FhirTransport transport;
    private final FhirAccessPolicy accessPolicy;
    private final FhirConsentPolicy consentPolicy;
    private final FhirRelationshipPolicy relationshipPolicy;
    private final AuditRecorder auditRecorder;
    private final FhirMutationJournal mutationJournal;
    private final EmergencyAccessReviewRecorder emergencyReviews;

    FhirAccessGatewayImpl(FhirTransport transport, FhirAccessPolicy accessPolicy, FhirConsentPolicy consentPolicy,
                          FhirRelationshipPolicy relationshipPolicy, AuditRecorder auditRecorder,
                          FhirMutationJournal mutationJournal, EmergencyAccessReviewRecorder emergencyReviews) {
        this.transport = transport;
        this.accessPolicy = accessPolicy;
        this.consentPolicy = consentPolicy;
        this.relationshipPolicy = relationshipPolicy;
        this.auditRecorder = auditRecorder;
        this.mutationJournal = mutationJournal;
        this.emergencyReviews = emergencyReviews;
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
    public Bundle search(String resourceType, Map<String, List<String>> parameters, String patientId) {
        String type = required(resourceType, "resourceType");
        Map<String, List<String>> safeParameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        return execute(FhirOperation.SEARCH, AuditAction.READ, type, null, patientId,
                () -> transport.search(type, safeParameters), ignored -> null);
    }

    @Override
    public Bundle searchAll(String resourceType, Map<String, List<String>> parameters, String patientId) {
        String type = required(resourceType, "resourceType");
        Map<String, List<String>> safeParameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        return execute(FhirOperation.SEARCH, AuditAction.READ, type, null, patientId,
                () -> transport.searchAll(type, safeParameters), ignored -> null);
    }

    @Override
    public Bundle history(String resourceType, String id, String patientId) {
        String type = required(resourceType, "resourceType");
        String resourceId = required(id, "id");
        return execute(FhirOperation.HISTORY, AuditAction.READ, type, resourceId, patientId,
                () -> transport.history(type, resourceId), ignored -> resourceId);
    }

    @Override
    public Bundle transaction(Bundle bundle, String patientId) {
        Objects.requireNonNull(bundle, "bundle");
        if (bundle.getType() != Bundle.BundleType.TRANSACTION) {
            throw new IllegalArgumentException("FHIR bundle must have transaction type");
        }
        Set<String> resourceTypes = new LinkedHashSet<>();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Bundle.HTTPVerb method = entry.getRequest().getMethod();
            String type = transactionResourceType(entry);
            String id = entry.getResource() == null ? null : entry.getResource().getIdElement().getIdPart();
            FhirOperation operation = switch (method) {
                case POST -> FhirOperation.CREATE;
                case PUT, PATCH -> FhirOperation.UPDATE;
                case DELETE -> FhirOperation.DELETE;
                case GET, HEAD -> FhirOperation.READ;
                default -> throw new IllegalArgumentException("Unsupported transaction method: " + method);
            };
            accessPolicy.authorize(operation, type, id, patientId);
            relationshipPolicy.authorize(type, patientId);
            consentPolicy.authorize(type, patientId);
            resourceTypes.add(type);
        }
        String auditedTypes = String.join(",", resourceTypes);
        emergencyReviews.record("Bundle", auditedTypes, patientId);
        return executeMutationAuthorized(FhirOperation.TRANSACTION, AuditAction.EXECUTE,
                "Bundle", auditedTypes, patientId,
                () -> transport.transaction(bundle), ignored -> auditedTypes);
    }

    @Override
    public MethodOutcome create(IBaseResource resource, String patientId) {
        Objects.requireNonNull(resource, "resource");
        String type = resource.fhirType();
        String requestedId = resource.getIdElement().getIdPart();
        return executeMutation(FhirOperation.CREATE, AuditAction.CREATE, type, requestedId, patientId,
                () -> transport.create(resource), outcome -> outcome.getId() == null
                        ? requestedId
                        : outcome.getId().getIdPart());
    }

    @Override
    public MethodOutcome update(IBaseResource resource, String patientId) {
        Objects.requireNonNull(resource, "resource");
        String id = required(resource.getIdElement().getIdPart(), "resource id");
        return executeMutation(FhirOperation.UPDATE, AuditAction.UPDATE, resource.fhirType(), id, patientId,
                () -> transport.update(resource), ignored -> id);
    }

    @Override
    public MethodOutcome delete(String resourceType, String id, String patientId) {
        return executeMutation(FhirOperation.DELETE, AuditAction.DELETE, required(resourceType, "resourceType"),
                required(id, "id"), patientId, () -> transport.delete(resourceType, id), ignored -> id);
    }

    private <T> T execute(FhirOperation operation, AuditAction auditAction, String resourceType,
                          String resourceId, String patientId, Supplier<T> request,
                          Function<T, String> auditedId) {
        T result;
        try {
            accessPolicy.authorize(operation, resourceType, resourceId, patientId);
            relationshipPolicy.authorize(resourceType, patientId);
            consentPolicy.authorize(resourceType, patientId);
            emergencyReviews.record(resourceType, resourceId, patientId);
            result = request.get();
        } catch (RuntimeException failure) {
            recordFailure(auditAction, resourceType, resourceId, patientId, failure);
            throw failure;
        }
        auditRecorder.record(success(auditAction, resourceType, auditedId.apply(result), patientId));
        return result;
    }

    private <T> T executeMutation(FhirOperation operation, AuditAction auditAction,
                                  String resourceType, String resourceId, String patientId,
                                  Supplier<T> request, Function<T, String> auditedId) {
        try {
            accessPolicy.authorize(operation, resourceType, resourceId, patientId);
            relationshipPolicy.authorize(resourceType, patientId);
            consentPolicy.authorize(resourceType, patientId);
            emergencyReviews.record(resourceType, resourceId, patientId);
        } catch (RuntimeException failure) {
            recordFailure(auditAction, resourceType, resourceId, patientId, failure);
            throw failure;
        }
        return executeMutationAuthorized(operation, auditAction, resourceType, resourceId,
                patientId, request, auditedId);
    }

    private <T> T executeMutationAuthorized(FhirOperation operation, AuditAction auditAction,
                                            String resourceType, String resourceId, String patientId,
                                            Supplier<T> request, Function<T, String> auditedId) {
        UUID journalId = mutationJournal.begin(operation, resourceType, resourceId, patientId);
        T result;
        String resolvedId;
        try {
            result = request.get();
            resolvedId = auditedId.apply(result);
            mutationJournal.remoteSucceeded(journalId, resolvedId);
        } catch (RuntimeException failure) {
            mutationJournal.remoteFailed(journalId, failure);
            recordFailure(auditAction, resourceType, resourceId, patientId, failure);
            throw failure;
        }
        try {
            auditRecorder.record(success(auditAction, resourceType, resolvedId, patientId));
            mutationJournal.audited(journalId);
        } catch (RuntimeException auditFailure) {
            throw new FhirAuditPendingException(journalId, auditFailure);
        }
        return result;
    }

    private void recordFailure(AuditAction action, String resourceType, String resourceId,
                               String patientId, RuntimeException originalFailure) {
        try {
            auditRecorder.record(new AuditEntry(action, resourceType, resourceId, patientId,
                    AuditOutcome.FAILURE, EmergencyAccessContext.active()));
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

    private static String transactionResourceType(Bundle.BundleEntryComponent entry) {
        if (entry.getResource() != null) {
            return entry.getResource().fhirType();
        }
        String url = entry.getRequest().getUrl();
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Transaction entry requires a resource or request URL");
        }
        int slash = url.indexOf('/');
        int query = url.indexOf('?');
        int end = slash >= 0 ? slash : query >= 0 ? query : url.length();
        return required(url.substring(0, end), "transaction resourceType");
    }

    private static AuditEntry success(AuditAction action, String resourceType, String resourceId,
                                      String patientId) {
        return new AuditEntry(action, resourceType, resourceId, patientId, AuditOutcome.SUCCESS,
                EmergencyAccessContext.active());
    }
}
