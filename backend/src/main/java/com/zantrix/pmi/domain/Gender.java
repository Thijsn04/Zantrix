package com.zantrix.pmi.domain;

/**
 * Enumeration representing the gender of a patient.
 * Aligns with FHIR AdministrativeGender value set.
 * Compliant with MDR/NEN7510 data minimization and standardization requirements.
 */
public enum Gender {
    /** Male gender. */
    MALE,
    /** Female gender. */
    FEMALE,
    /** Other gender identity. */
    OTHER,
    /** Gender is unknown or not disclosed. */
    UNKNOWN
}
