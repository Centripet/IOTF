package org.iotf.requestFormation.collect_analyze;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record alarmLogListRequest(
        @NotNull @Min(1) @Max(100) Integer page,
        @NotNull @Min(1) @Max(100) Integer size
) {
}
