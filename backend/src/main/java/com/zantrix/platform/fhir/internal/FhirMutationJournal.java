package com.zantrix.platform.fhir.internal;

import java.util.UUID;

interface FhirMutationJournal {

    UUID begin(FhirOperation operation, String resourceType, String resourceId, String patientId);

    void remoteSucceeded(UUID id, String resolvedResourceId);

    void audited(UUID id);

    void remoteFailed(UUID id, Throwable failure);
}
