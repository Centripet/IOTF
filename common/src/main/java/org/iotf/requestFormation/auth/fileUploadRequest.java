package org.iotf.requestFormation.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record fileUploadRequest(
        @NotNull @NotBlank String suffix,
        Integer MAX_SIZE_FLAG
) {
}
