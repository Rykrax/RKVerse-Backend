package dev.rykrax.rkverse.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "Username đã tồn tại"),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "Role không tồn tại"),
    REFRESH_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "Refresh token không được để trống"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Refresh token không tồn tại"),
    REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "Refresh token đã bị thu hồi"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh token đã hết hạn");

    private final HttpStatus httpStatus;
    private final String message;
}