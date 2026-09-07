package dev.rykrax.rkverse.feature.auth;

import dev.rykrax.rkverse.feature.auth.dto.request.LoginRequest;
import dev.rykrax.rkverse.feature.auth.dto.request.RegisterRequest;
import dev.rykrax.rkverse.feature.auth.dto.response.LoginResponse;

public interface IAuthService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
}
