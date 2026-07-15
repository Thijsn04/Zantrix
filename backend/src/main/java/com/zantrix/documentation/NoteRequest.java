package com.zantrix.documentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record NoteRequest(
        @NotBlank String patientId,
        @NotBlank String encounterId,
        @NotBlank String authorId,
        @NotBlank String typeSystem,
        @NotBlank String typeCode,
        @NotBlank String typeDisplay,
        @NotBlank String title,
        @NotEmpty List<@Valid NoteSection> sections) {
}
