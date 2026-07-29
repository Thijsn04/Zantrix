package com.zantrix.care;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CareTeamRequest(
        @NotBlank String patientId,
        @NotBlank String name,
        @NotEmpty List<@jakarta.validation.Valid Member> members) {

    public record Member(@NotBlank String practitionerId, @NotBlank String roleCode,
                         @NotBlank String roleDisplay) { }
}
