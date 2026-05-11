package org.iotf.collectanalyzeservice.controller.requestFormation;

import java.time.Instant;

public record fluxQueryRequest(
        String deviceId,
        Instant start,
        Instant end
) {
}
