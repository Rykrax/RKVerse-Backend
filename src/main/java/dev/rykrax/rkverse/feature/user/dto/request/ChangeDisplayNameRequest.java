package dev.rykrax.rkverse.feature.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeDisplayNameRequest(
        @NotBlank(message = "Không được để trống")
        @Size(min = 5, max = 100, message = "Tên hiển thị chỉ chỉ được phép từ 5 đến 100 kí tự")
        String displayName
) {
}
