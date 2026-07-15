package com.zantrix.documentation;

import jakarta.validation.constraints.NotBlank;

public record NoteSection(@NotBlank String codeSystem, @NotBlank String code,
                          @NotBlank String display, @NotBlank String text) { }
