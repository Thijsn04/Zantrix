package com.zantrix.platform.fhir.internal;

/**
 * Whether the acting clinician has a reason to be in this patient's record.
 *
 * Roles and SMART scopes answer "may this kind of user do this kind of thing".
 * They do not answer "does this user have anything to do with this patient",
 * and without that a role check permits an entire population.
 */
interface FhirRelationshipPolicy {

    void authorize(String resourceType, String patientId);
}
