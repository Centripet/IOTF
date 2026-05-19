package org.iotf.requestFormation.collect_analyze;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record energyAggByDeviceRequest(
    @NotNull Long device_id,
    // period:hourly/daily/weekly/monthly/yearly
    @NotNull @NotBlank String period
) {
}
