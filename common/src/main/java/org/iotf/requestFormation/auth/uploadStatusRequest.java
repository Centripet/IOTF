package org.iotf.requestFormation.auth;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record uploadStatusRequest(
        @NotNull List<uploadSubmitRequest> files
) {
}
