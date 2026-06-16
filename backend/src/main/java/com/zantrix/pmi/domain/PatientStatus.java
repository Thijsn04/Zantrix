package com.zantrix.pmi.domain;

/**
 * Enumeration representing the current status of a patient record.
 * Supports maintaining accurate patient life-cycle data in compliance with NEN7510.
 */
public enum PatientStatus {
    /** Active patient record. */
    ACTIVE,
    /** Patient is deceased. */
    DECEASED,
    /** Patient record has been merged into another record to resolve duplicates. */
    MERGED
}
