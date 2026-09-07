package dev.rykrax.rkverse.feature.auth;

public interface IRefreshTokenService {
    RefreshToken createRefreshToken(Long useId);
    RefreshToken rotateRefreshToken(String token);
    void logoutCurrent(String token);
    void revokeAllUserTokens(Long userId);
}
