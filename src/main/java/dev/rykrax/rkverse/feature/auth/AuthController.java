package dev.rykrax.rkverse.feature.auth;

import dev.rykrax.rkverse.common.ApiResponse;
import dev.rykrax.rkverse.feature.auth.dto.request.LoginRequest;
import dev.rykrax.rkverse.feature.auth.dto.response.LoginResponse;
import dev.rykrax.rkverse.feature.auth.dto.response.RefreshTokenResponse;
import dev.rykrax.rkverse.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;
    private final AuthService authService;
    private final UserDetailsService userDetailsService;
    @Value("${jwt.refresh-token.expiration-seconds:604800000}")
    private long refreshTokenExpirationSeconds;

    @GetMapping("/token")
    public String getToken() {
        UserDetails user = userDetailsService.loadUserByUsername("admin");
        return jwtService.generateToken(user);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest
            , HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(loginRequest);

        // tạo HttpOnly Cookie chứa refreshToken
        ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResponse.refreshToken())
                .httpOnly(true)                    // ngăn chặn js (XSS)
                .secure(false)                     // đổi thành true khi deploy production (HTTPS)
                .path("/api/v1/auth")             // chỉ gửi cookie này đến các endpoint auth
                .maxAge(refreshTokenExpirationSeconds / 1000)
                .sameSite("Strict")                // chống CSRF
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.success(loginResponse);
    }

    @PostMapping("/refresh-token")
    public ApiResponse<RefreshTokenResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Refresh token không tồn tại trong cookie");
        }

        RefreshTokenResponse tokenResponse = authService.refreshToken(refreshToken);
        return ApiResponse.success(tokenResponse);
    }
}