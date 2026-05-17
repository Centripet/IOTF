package org.iotf.requestFormation.collect_analyze;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record deviceSubmitRequest(
    @NotNull @NotBlank String device_UUID,
    @NotNull @NotBlank String device_type,
    @NotNull @NotBlank String device_name,
//    @Max(9600) @Min(30) Integer frequency,
    Float overload_threshold,
    Float high_energy_threshold,
    Float current_threshold
) {
}
