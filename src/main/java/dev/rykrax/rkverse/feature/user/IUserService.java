package dev.rykrax.rkverse.feature.user;

import dev.rykrax.rkverse.feature.user.dto.response.UserResponse;

import java.util.List;

public interface IUserService {
    List<UserResponse> getUsers();
}
