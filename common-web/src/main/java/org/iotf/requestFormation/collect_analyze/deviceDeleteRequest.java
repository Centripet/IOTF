package org.iotf.requestFormation.collect_analyze;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record deviceDeleteRequest(
        @NotNull Long device_id
) {
}
