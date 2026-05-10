package org.iotf.requestFormation.auth;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record fileSubmitRequest(
        @NotNull List<uploadSubmitRequest> files
) {
}
