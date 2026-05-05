package org.iotf.requestFormation.test;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record testRequest(
        @NotNull @NotBlank String x,
        @NotNull @NotBlank String y
) {
}
