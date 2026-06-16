package com.zantrix.scheduling.domain;

/**
 * Represents the status of an encounter.
 * Mapped to FHIR EncounterStatus values.
 */
public enum EncounterStatus {
    PLANNED,
    ARRIVED,
    TRIAGED,
    IN_PROGRESS,
    ONLEAVE,
    FINISHED,
    CANCELLED,
    ENTERED_IN_ERROR,
    UNKNOWN
}
