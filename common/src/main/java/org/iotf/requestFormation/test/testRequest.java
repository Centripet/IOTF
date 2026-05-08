package org.iotf.requestFormation.test;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;

public record testRequest(
        @NonNull @NotBlank String user_id,
        @NotNull @NotBlank String x,
        @NotNull @NotBlank String y
) {
}
