package dev.rykrax.rkverse.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) // don't response field if null
public record ApiResponse<T> (
    int status,
    String message,
    T data
) {
    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(200, "Success", result);
    }

    public static <T> ApiResponse<T> success(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
    public static <T> ApiResponse<T> success(int status, String message, T result) {
        return new ApiResponse<>(status, message, result);
    }

    public static <T> ApiResponse<T> error(int statusCode, String message) {
        return new ApiResponse<>(statusCode, message, null);
    }

    public static <T> ApiResponse<T> error(int statusCode, T error) {
        return new ApiResponse<>(statusCode, null, error);
    }
    public static <T> ApiResponse<T> error(int statusCode, String message, T error) {
        return new ApiResponse<>(statusCode, message, error);
    }
}
