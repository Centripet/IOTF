package org.iotf.requestFormation.collect_analyze;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record reportModifyRequest(
        @NotNull @NotBlank Long device_id,
        @NotBlank String device_UUID,
        @NotBlank String device_type,
        @NotBlank String device_name,
//        @Max(9600) @Min(30) Integer frequency,
        Float overload_threshold,
        Float high_energy_threshold,
        Float current_threshold
) {
}
