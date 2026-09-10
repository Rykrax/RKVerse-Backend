package dev.rykrax.rkverse.feature.user;

import dev.rykrax.rkverse.common.ApiResponse;
import dev.rykrax.rkverse.feature.user.dto.request.ChangePasswordRequest;
import dev.rykrax.rkverse.feature.user.dto.response.UserResponse;
import dev.rykrax.rkverse.security.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping()
    @PreAuthorize("hasAuthority('user.select')")
    public ApiResponse<List<UserResponse>> getUsers() {
        List<UserResponse> users = userService.getUsers();
        return new ApiResponse<>(200, "Danh sách users", users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user.select')")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse userResponse = userService.getUserById(id);
        return new ApiResponse<>(200, "Thông tin user", userResponse);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getProfile(@AuthenticationPrincipal CustomUserDetail userDetails) {
        UserResponse userResponse = userService.getMe(userDetails);
        return new ApiResponse<>(200, "Thông tin người dùng", userResponse);
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable Long id) {

        return new ApiResponse<>(200, "Cập nhật thành công", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return new ApiResponse<>(201, "Xóa thành công", null);
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                            @AuthenticationPrincipal CustomUserDetail userDetails) {
        userService.changePassword(userDetails.getId(), request);
        return new ApiResponse<>(200, "Đổi mật khẩu thành công", null);
    }
}
