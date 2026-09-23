package dev.rykrax.rkverse.feature.user;

import dev.rykrax.rkverse.feature.role.Role;
import dev.rykrax.rkverse.feature.user.dto.request.UserRequest;
import dev.rykrax.rkverse.feature.user.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    User toEntity(UserRequest userRequest);

    UserResponse toResponse(User user);

    default String map(Role role) {
        return role != null ? role.getName() : null;
    }
}
