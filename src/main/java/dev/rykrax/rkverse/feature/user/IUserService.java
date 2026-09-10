package dev.rykrax.rkverse.feature.user;

import dev.rykrax.rkverse.feature.user.dto.request.ChangePasswordRequest;
import dev.rykrax.rkverse.feature.user.dto.response.UserResponse;
import dev.rykrax.rkverse.security.CustomUserDetail;

import java.util.List;

public interface IUserService {
    List<UserResponse> getUsers();
    UserResponse getUserById(Long id);
    UserResponse getMe(CustomUserDetail userDetails);
    UserResponse update(Long id);
    void delete(Long id);
    void changePassword(Long userId, ChangePasswordRequest request);
}
