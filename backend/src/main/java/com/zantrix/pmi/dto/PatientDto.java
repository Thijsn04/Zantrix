package com.zantrix.pmi.dto;

import com.zantrix.pmi.domain.Gender;
import com.zantrix.pmi.domain.PatientStatus;

import java.time.LocalDate;
import java.util.UUID;

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
