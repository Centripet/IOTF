package org.iotf.requestFormation.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record loginRequest(
        @NotNull @NotBlank String user_name,
        @NotNull @NotBlank String passwordHash,
        String device
) {
}
