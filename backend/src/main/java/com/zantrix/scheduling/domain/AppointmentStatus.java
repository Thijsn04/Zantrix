package com.zantrix.scheduling.domain;

/**
 * Represents the status of an appointment within the Zantrix EPD.
 * <p>
 * These statuses are mapped to the HL7 FHIR AppointmentStatus value set where possible,
 * enabling standardized interoperability and workflow tracking.
 * </p>
 */
public enum AppointmentStatus {
    /** The appointment has been proposed but not yet confirmed. */
    PENDING,
    /** The appointment has been confirmed and scheduled. */
    BOOKED,
    /** The patient has arrived at the clinic for the appointment. */
    ARRIVED,
    /** The appointment has been successfully completed. */
    FULFILLED,
    /** The appointment was cancelled before it took place. */
    CANCELLED,
    /** The patient did not show up for the appointment. */
    NOSHOW,
    /** The appointment was created by mistake and should be considered invalid. */
    ENTERED_IN_ERROR
}
