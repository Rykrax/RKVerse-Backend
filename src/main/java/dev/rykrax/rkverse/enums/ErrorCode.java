package dev.rykrax.rkverse.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "Username đã tồn tại"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User không tồn tại"),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "Role không tồn tại"),
    REFRESH_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "Refresh token không được để trống"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Refresh token không tồn tại"),
    REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "Refresh token đã bị thu hồi"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh token đã hết hạn"),
    ACCOUNT_NOT_ACTIVE(HttpStatus.UNAUTHORIZED, "Tài khoản chưa được kích hoạt hoặc đã bị khóa"),
    CURRENT_PASSWORD_INCORRECT(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không chính xác"),
    NEW_PASSWORD_MUST_BE_DIFFERENT(HttpStatus.CONFLICT, "Mật khẩu mới không được trùng với mật khẩu hiện tại"),

    // Comic
    COMIC_NOT_FOUND(HttpStatus.NOT_FOUND, "Truyện không tồn tại"),

    // Chapter
    CHAPTER_ALREADY_EXISTS(HttpStatus.CONFLICT, "Chương đã tồn tại"),

    //
    SOMETHING_WRONG(HttpStatus.BAD_REQUEST, "Something wrong"),
    WRONG_TOKEN(HttpStatus.BAD_REQUEST, "Thời gian không phù hợp hoặc token không hợp lệ");

    private final HttpStatus httpStatus;
    private final String message;
}