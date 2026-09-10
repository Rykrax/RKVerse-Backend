package dev.rykrax.rkverse.feature.user;

import dev.rykrax.rkverse.common.ApiResponse;
import dev.rykrax.rkverse.feature.user.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping()
    public ResponseEntity<List<UserResponse>> getUsers() {
        return new ResponseEntity<>(userService.getUsers(), HttpStatus.OK);
    }

    @GetMapping("/u")
    public ApiResponse<List<UserResponse>> getUsersInfo() {
        return new ApiResponse<>(200, "Danh sách user", userService.getUsers());
    }

//    @GetMapping("/profile")
//    public ApiResponse<String> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
//        return  userDetails.getUsername();
//    }
}
