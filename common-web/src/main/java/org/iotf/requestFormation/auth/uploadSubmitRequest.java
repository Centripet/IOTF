package org.iotf.requestFormation.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record uploadSubmitRequest(
        @NotNull @NotBlank String key,
        @NotNull @NotBlank String suffix,
        @NotNull @NotBlank String origin_name,
        @NotNull Integer type,
        String title,
        String info,
        String typeInfo
        ) {
}
