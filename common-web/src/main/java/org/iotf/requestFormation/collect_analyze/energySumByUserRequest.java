package org.iotf.requestFormation.collect_analyze;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record energySumByUserRequest(
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime
) {
}
