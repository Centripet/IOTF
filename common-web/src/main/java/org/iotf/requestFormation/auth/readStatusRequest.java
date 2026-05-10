package org.iotf.requestFormation.auth;

import jakarta.validation.constraints.NotNull;

public record readStatusRequest(
        @NotNull  String messageId
) {
}
