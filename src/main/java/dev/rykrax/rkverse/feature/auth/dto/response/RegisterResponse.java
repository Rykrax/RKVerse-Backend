package dev.rykrax.rkverse.feature.auth.dto.response;

public record RegisterResponse (
        Long id,
        String username,
        String password
) {
}
