package dev.rykrax.rkverse.feature.user.dto.response;

public record UserResponse (
        Long id,
        String username,
        String email
) {
}
