package org.iotf.requestFormation.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record resetPasswordRequest(
        @NotNull @NotBlank String passwordHashOld,
        @NotNull @NotBlank String passwordHash,
        @NotNull @NotBlank String passwordHashRe
) {
}
