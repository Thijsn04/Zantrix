package com.zantrix.scheduling.domain;

/**
 * Represents the status of an appointment.
 * Mapped to FHIR AppointmentStatus values where possible.
 */
public enum AppointmentStatus {
    PENDING,
    BOOKED,
    ARRIVED,
    FULFILLED,
    CANCELLED,
    NOSHOW,
    ENTERED_IN_ERROR
}
