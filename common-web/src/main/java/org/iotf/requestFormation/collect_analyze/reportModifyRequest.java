package org.iotf.requestFormation.collect_analyze;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record reportModifyRequest(
        @NotNull Long device_id,
        String device_UUID,
        String device_type,
        String device_name,
//        @Max(9600) @Min(30) Integer frequency,
        Float overload_threshold,
        Float high_energy_threshold,
        Float current_threshold
) {
}
