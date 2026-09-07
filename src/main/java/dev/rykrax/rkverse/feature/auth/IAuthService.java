package dev.rykrax.rkverse.feature.auth;

import dev.rykrax.rkverse.feature.auth.dto.request.LoginRequest;
import dev.rykrax.rkverse.feature.auth.dto.request.RegisterRequest;
import dev.rykrax.rkverse.feature.auth.dto.response.LoginResponse;
import dev.rykrax.rkverse.feature.auth.dto.response.RefreshTokenResponse;

public interface IAuthService {
    LoginResponse login(LoginRequest request);
    RefreshTokenResponse refreshToken(String refreshToken);
    void register(RegisterRequest request);
}
