package com.zantrix.pmi.dto;

import com.zantrix.pmi.domain.Gender;
import com.zantrix.pmi.domain.PatientStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Data Transfer Object representing a patient record.
 * Used for safely returning patient data through API endpoints, ensuring NEN7510 compliance.
 *
 * @param id               the unique identifier of the patient
 * @param bsn              the Citizen Service Number
 * @param firstName        the patient's first name
 * @param lastName         the patient's last name
 * @param birthDate        the patient's date of birth
 * @param gender           the patient's gender
 * @param status           the current status of the patient record
 * @param isEmergency      indicates if this was an emergency creation
 * @param insuranceCompany the insurance company name
 * @param insuranceNumber  the insurance policy number
 * @param mergedIntoId     the ID of another patient if this record was merged
 * @param fhirDataJson     the FHIR Patient resource represented as a JSON string
 */
public record PatientDto(
    UUID id,
    String bsn,
    String firstName,
    String lastName,
    LocalDate birthDate,
    Gender gender,
    PatientStatus status,
    boolean isEmergency,
    String insuranceCompany,
    String insuranceNumber,
    UUID mergedIntoId,
    String fhirDataJson
) {}
