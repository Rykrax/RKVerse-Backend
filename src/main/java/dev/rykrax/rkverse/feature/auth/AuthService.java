package dev.rykrax.rkverse.feature.auth;

import dev.rykrax.rkverse.feature.auth.IAuthService;
import dev.rykrax.rkverse.feature.auth.dto.request.LoginRequest;
import dev.rykrax.rkverse.feature.auth.dto.request.RegisterRequest;
import dev.rykrax.rkverse.feature.auth.dto.response.LoginResponse;
import dev.rykrax.rkverse.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(userDetails);
        return new LoginResponse(accessToken);
    }

    @Override
    public void register(RegisterRequest request) {

    }
}