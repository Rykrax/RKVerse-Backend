package dev.rykrax.rkverse.feature.user;

import dev.rykrax.rkverse.enums.ErrorCode;
import dev.rykrax.rkverse.enums.UserStatus;
import dev.rykrax.rkverse.exception.AppException;
import dev.rykrax.rkverse.feature.auth.RefreshTokenService;
import dev.rykrax.rkverse.feature.user.dto.request.ChangePasswordRequest;
import dev.rykrax.rkverse.feature.user.dto.response.UserResponse;
import dev.rykrax.rkverse.security.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Override
    public List<UserResponse> getUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(userMapper::toResponse).toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getMe(CustomUserDetail userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse update(Long id) {
        return null;
    }

    @Override
    public void delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_FOUND));
        user.setDeletedAt(Instant.now());
        userRepository.save(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // check user and status
        User user = userRepository.findById(userId).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }

        // check current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.CURRENT_PASSWORD_INCORRECT);
        }

        // compare current password and new password
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.NEW_PASSWORD_MUST_BE_DIFFERENT);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        refreshTokenService.revokeAllUserTokens(userId);
    }
}
