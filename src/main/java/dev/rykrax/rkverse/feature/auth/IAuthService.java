package dev.rykrax.rkverse.feature.auth;

import dev.rykrax.rkverse.feature.auth.dto.request.LoginRequest;
import dev.rykrax.rkverse.feature.auth.dto.request.RegisterRequest;

public interface IAuthService {
    void login(LoginRequest request);
    void register(RegisterRequest request);
}
