package org.iotf.requestFormation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record energyAggListByUserRequest(
        @NotNull @Min(1) @Max(100) Integer page,
        @NotNull @Min(1) @Max(100) Integer size,

        @NotNull @NotBlank String period
) {
}
