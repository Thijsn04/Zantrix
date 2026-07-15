package com.zantrix.administration;

import jakarta.validation.constraints.NotBlank;

public record LocationRequest(@NotBlank String organizationId, @NotBlank String name,
                              @NotBlank String typeCode, @NotBlank String typeDisplay,
                              String addressLine, String city, String postalCode, String country) { }
