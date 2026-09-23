package dev.rykrax.rkverse.feature.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse (
        Long id,
        String username,
        String email,
        String displayName,
        Set<String> roles
) {
}
