package com.zantrix.platform.fhir.internal;

interface FhirAccessPolicy {

    void authorize(FhirOperation operation, String resourceType, String resourceId, String patientId);
}
