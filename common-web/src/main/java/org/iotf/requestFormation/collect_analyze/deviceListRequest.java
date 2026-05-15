package org.iotf.requestFormation.collect_analyze;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record deviceListRequest(
        @NotNull @Min(1) @Max(100) Integer page,
        @NotNull @Min(1) @Max(100) Integer size,

        String device_type,
        String device_name
) {
}
