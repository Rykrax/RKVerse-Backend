package dev.rykrax.rkverse.feature.auth;

import dev.rykrax.rkverse.feature.auth.IAuthService;
import dev.rykrax.rkverse.feature.auth.dto.request.LoginRequest;
import dev.rykrax.rkverse.feature.auth.dto.request.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {

    @Override
    public void login(LoginRequest request) {

    }

    @Override
    public void register(RegisterRequest request) {

    }
}