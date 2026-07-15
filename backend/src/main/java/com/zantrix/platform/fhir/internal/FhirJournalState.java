package com.zantrix.platform.fhir.internal;

enum FhirJournalState {
    PENDING,
    REMOTE_SUCCEEDED,
    AUDITED,
    REMOTE_FAILED
}
