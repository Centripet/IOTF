package org.iotf.requestFormation.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record loginCaptchaRequest(
        @NotNull @NotBlank String account,
        @NotNull @NotBlank String verificationCode,
        String device
) {
}
