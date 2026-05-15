package org.iotf.requestFormation.collect_analyze;

import jakarta.validation.constraints.NotNull;

public record acknowledgeRequest(
        @NotNull Long alarm_id
) {
}
