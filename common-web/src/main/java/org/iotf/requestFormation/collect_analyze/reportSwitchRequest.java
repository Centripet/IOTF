package org.iotf.requestFormation.collect_analyze;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record reportSwitchRequest(
        @NotNull @NotBlank Long device_id,
        @NotNull Boolean report_status
) {
}
