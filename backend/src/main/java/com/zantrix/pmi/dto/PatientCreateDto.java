package com.zantrix.pmi.dto;

import com.zantrix.pmi.domain.Gender;
import java.time.LocalDate;

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
