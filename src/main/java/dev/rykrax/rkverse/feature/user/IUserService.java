package dev.rykrax.rkverse.feature.user;

import dev.rykrax.rkverse.common.PageResponse;
import dev.rykrax.rkverse.feature.user.dto.request.ChangePasswordRequest;
import dev.rykrax.rkverse.feature.user.dto.response.UserResponse;
import dev.rykrax.rkverse.security.CustomUserDetail;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IUserService {
    PageResponse<UserResponse> getUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse getMe(CustomUserDetail userDetails);
    UserResponse update(Long id);
    void delete(Long id);
    void changePassword(Long userId, ChangePasswordRequest request);
}
