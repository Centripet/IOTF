package org.iotf.requestFormation.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record sendCodeRequest(
        @NotNull @NotBlank String str,
        @NotNull @NotBlank  String method
) {
}
