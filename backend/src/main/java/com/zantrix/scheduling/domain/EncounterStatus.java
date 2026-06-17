package com.zantrix.scheduling.domain;

/**
 * Represents the status of a patient encounter in the Zantrix EPD.
 * <p>
 * This enumeration aligns tightly with the HL7 FHIR EncounterStatus value set to ensure
 * standardized tracking of the patient's journey from arrival to discharge.
 * </p>
 */
public enum EncounterStatus {
    /** The encounter is planned but has not yet commenced. */
    PLANNED,
    /** The patient has arrived at the healthcare facility. */
    ARRIVED,
    /** The patient has undergone triage assessment. */
    TRIAGED,
    /** The encounter is currently active and the patient is receiving care. */
    IN_PROGRESS,
    /** The patient is temporarily absent from the facility. */
    ONLEAVE,
    /** The encounter has concluded, and the patient has been discharged. */
    FINISHED,
    /** The planned encounter was cancelled. */
    CANCELLED,
    /** The encounter was recorded in error. */
    ENTERED_IN_ERROR,
    /** The current status of the encounter is unknown. */
    UNKNOWN
}
