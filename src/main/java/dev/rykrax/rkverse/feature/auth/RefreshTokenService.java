package dev.rykrax.rkverse.feature.auth;

import dev.rykrax.rkverse.feature.user.User;
import dev.rykrax.rkverse.feature.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements IRefreshTokenService {
    @Value("${jwt.refresh-token}")
    private Long refreshTokenExpiration;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User userProxy = userRepository.getReferenceById(userId);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(userProxy)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public RefreshToken rotateRefreshToken(String token) {
        RefreshToken existingToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh Token không tồn tại"));

        // phát hiện token bị đánh cắp và sử dụng lại
        if (existingToken.getRevoked()) {
            refreshTokenRepository.deleteAllByUserId(existingToken.getUser().getId());
            throw new RuntimeException("Cảnh báo bảo mật: Token đã bị sử dụng lại. Vui lòng đăng nhập lại!");
        }

        // kiểm tra token đã hết hạn
        if (existingToken.isExpired()) {
            existingToken.setRevoked(true);
            refreshTokenRepository.save(existingToken);
            throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại!");
        }

        existingToken.setRevoked(true);
        refreshTokenRepository.save(existingToken);

        return createRefreshToken(existingToken.getUser().getId());
    }

    @Override
    @Transactional
    public void logoutCurrent(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // Kiểm tra expire của token
    public RefreshToken verifyExpiration(RefreshToken refreshToken) {
        // kiểm tra nếu đã bị revoked từ trước
        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token đã bị vô hiệu hóa!");
        }

        // kiểm tra thời gian hết hạn
        if (refreshToken.isExpired()) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại!");
        }

        return refreshToken;
    }
}
