package com.zantrix.platform.fhir.internal;

interface FhirConsentPolicy {
    void authorize(String resourceType, String patientId);
}
