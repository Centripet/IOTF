package org.iotf.requestFormation.collect_analyze;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record energyAggListByDeviceRequest(
        @NotNull @Min(1) @Max(100) Integer page,
        @NotNull @Min(1) @Max(100) Integer size,

        @NotNull Long device_id,
        @NotNull @NotBlank String period
) {
}
