package org.iotf.requestFormation.collect_analyze;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record alarmListRequest(
        @NotNull @Min(1) @Max(100) Integer page,
        @NotNull @Min(1) @Max(100) Integer size,

        Long device_id,
        String alarm_type,
        String device_name,
        String status
) {
}
