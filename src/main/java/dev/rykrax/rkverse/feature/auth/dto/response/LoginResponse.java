package dev.rykrax.rkverse.feature.auth.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
