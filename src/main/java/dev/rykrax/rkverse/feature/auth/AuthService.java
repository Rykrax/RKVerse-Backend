package dev.rykrax.rkverse.feature.auth;

import dev.rykrax.rkverse.enums.ErrorCode;
import dev.rykrax.rkverse.exception.AppException;
import dev.rykrax.rkverse.feature.auth.dto.request.LoginRequest;
import dev.rykrax.rkverse.feature.auth.dto.request.RegisterRequest;
import dev.rykrax.rkverse.feature.auth.dto.response.LoginResponse;
import dev.rykrax.rkverse.feature.auth.dto.response.RefreshTokenResponse;
import dev.rykrax.rkverse.feature.auth.dto.response.RegisterResponse;
import dev.rykrax.rkverse.feature.role.Role;
import dev.rykrax.rkverse.feature.role.RoleRepository;
import dev.rykrax.rkverse.feature.user.User;
import dev.rykrax.rkverse.feature.user.UserRepository;
import dev.rykrax.rkverse.security.CustomUserDetail;
import dev.rykrax.rkverse.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        assert userDetails != null;
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        return new LoginResponse(accessToken, refreshToken.getToken());
    }

    @Override
    public RefreshTokenResponse refreshToken(String token) {
        if (token == null) {
            throw new RuntimeException("Refresh token not null");
        }

        RefreshToken refreshToken = refreshTokenService.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại"));

        if (refreshToken.getRevoked()) {
            throw new RuntimeException("Refresh token is revoked");
        }

        RefreshToken verifyToken = refreshTokenService.verifyExpiration(refreshToken);
        User user = verifyToken.getUser();
        CustomUserDetail userDetail = new CustomUserDetail(user);
        String newAccessToken = jwtService.generateToken(userDetail);

        return new RefreshTokenResponse(newAccessToken);
    }


    @Override
    public RegisterResponse register(RegisterRequest request) {
        System.out.println(request);
        if (userRepository.existsByUsername(request.username())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        String passwordHash = passwordEncoder.encode(request.password());

        Role defaultRole = roleRepository.findByName("USER").orElseThrow(() ->
                new RuntimeException("Role không tồn tại")
        );

        User newUser = User.createRegisteredUser(
                request.username(),
                passwordHash,
                defaultRole
        );

        User user = userRepository.save(newUser);
        return new RegisterResponse(user.getUsername());
    }

    @Override
    public void logout(String authorization) {
        String token = authorization.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_FOUND));
        refreshTokenService.revokeAllUserTokens(user.getId());
    }
}