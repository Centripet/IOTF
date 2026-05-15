package org.iotf.requestFormation.collect_analyze;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record reportModifyRequest(
        @NotNull @NotBlank Long device_id,
        String device_UUID,
        String device_type,
        String device_name,
        Integer frequency
) {
}
