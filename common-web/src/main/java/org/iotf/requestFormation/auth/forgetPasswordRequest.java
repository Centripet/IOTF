package org.iotf.requestFormation.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record forgetPasswordRequest(
        @NotNull @NotBlank String account,
        @NotNull @NotBlank String verificationCode,
        @NotNull @NotBlank String passwordHash
//        @NotNull @NotBlank String passwordHashRe
) {
}
