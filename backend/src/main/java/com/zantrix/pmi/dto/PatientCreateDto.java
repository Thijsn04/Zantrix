package com.zantrix.pmi.dto;

import com.zantrix.pmi.domain.Gender;
import java.time.LocalDate;

/**
 * Data Transfer Object for creating a new patient record.
 * Designed to minimize data exposure (MDR/NEN7510) and separate API contracts from internal entities.
 *
 * @param bsn              the Citizen Service Number
 * @param firstName        the patient's first name
 * @param lastName         the patient's last name
 * @param birthDate        the patient's date of birth
 * @param gender           the patient's gender
 * @param isEmergency      indicates if the patient is created under emergency circumstances (bypasses strict BSN checks)
 * @param insuranceCompany the patient's insurance company name
 * @param insuranceNumber  the patient's insurance policy number
 * @param fhirDataJson     additional patient demographics in FHIR JSON format
 */
public record PatientCreateDto(
    String bsn,
    String firstName,
    String lastName,
    LocalDate birthDate,
    Gender gender,
    boolean isEmergency,
    String insuranceCompany,
    String insuranceNumber,
    String fhirDataJson
) {}
