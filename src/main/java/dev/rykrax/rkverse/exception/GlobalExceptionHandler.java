package dev.rykrax.rkverse.exception;

import dev.rykrax.rkverse.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Dữ liệu gửi lên không hợp lệ",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Map<String,String>>> handleAppException(AppException ex) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("error", ex.getErrorCode().name());

        ApiResponse<Map<String,String>> response = ApiResponse.error(
                ex.getErrorCode().getHttpStatus().value(),
                ex.getMessage(),
                errorDetails
        );

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Xác thực thất bại: {}", ex.getMessage());

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                "Tên đăng nhập hoặc mật khẩu không chính xác",
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
