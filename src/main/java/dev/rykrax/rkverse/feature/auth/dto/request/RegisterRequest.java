package dev.rykrax.rkverse.feature.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest (
        @NotBlank(message = "Username không được để trống")
        String username,

        @NotBlank(message = "Mật khẩu không được để trống")
        String password,
        String confirmPassword
) {
}
