package com.zantrix.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientRegistration(
        @NotBlank @Size(max = 100) String givenName,
        @NotBlank @Size(max = 100) String familyName,
        @NotNull @PastOrPresent LocalDate birthDate,
        @NotBlank String administrativeGender,
        @Size(max = 255) String email,
        @Size(max = 64) String phone,
        @Size(max = 255) String identifierSystem,
        @Size(max = 255) String identifierValue,
        boolean confirmedUnique) {
}
