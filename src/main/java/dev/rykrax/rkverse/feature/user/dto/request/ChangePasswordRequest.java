package dev.rykrax.rkverse.feature.user.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Mật khẩu hiện tại không được để trống")
        String currentPassword,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, max = 64, message = "Mật khẩu phải từ 8 đến 64 ký tự")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Mật khẩu phải chứa ít nhất 1 chữ thường, 1 chữ hoa, 1 số và 1 ký tự đặc biệt (@$!%*?&)"
        )
        String newPassword,

        @NotBlank(message = "Xác nhận mật khẩu không được để trống")
        String confirmPassword
) {
    @AssertTrue(message = "Mật khẩu xác nhận không khớp")
    public boolean isPasswordMatching() {
        if (confirmPassword == null || confirmPassword.isBlank()) {
            return true;
        }
        return confirmPassword.equals(newPassword);
    }
}
