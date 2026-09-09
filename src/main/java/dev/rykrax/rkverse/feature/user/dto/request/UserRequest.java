package dev.rykrax.rkverse.feature.user.dto.request;

public record UserRequest (
        String username,
        String password,
        String confirmPassword
) {
}
