package org.iotf.requestFormation.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record registerRequest(
        @NotNull
        @NotBlank(message = "账号不能为空")
        @Size(min = 8, max = 20, message = "账号长度必须在8到20位之间")
        String user_name,
        @Size(min = 6, max = 20, message = "密码长度必须在6到20位之间")
        @NotNull String passwordHash,
        @NotNull String phone,
        @NotNull String verificationCode,
        @NotNull Boolean agree_policy
) {
}
