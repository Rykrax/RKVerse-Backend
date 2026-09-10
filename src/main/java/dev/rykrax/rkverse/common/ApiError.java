package dev.rykrax.rkverse.common;

public record ApiError<T> (
        int statusCode,
        String message,
        T data
) {
    public static <T> ApiError<T> BabRequest(String message) {
        return new ApiError<>(400, message, null);
    }

    public static <T> ApiError<T> Unauthorized(String message) {
        return new ApiError<>(401, message, null);
    }

    public static <T> ApiError<T> Forbidden(String message) {
        return new ApiError<>(402, message, null);
    }

    public static <T> ApiError<T> NotFound(String message) {
        return new ApiError<>(404, message, null);
    }

    public static <T> ApiError<T> TooManyRequests(String message) {
        return new ApiError<>(429, message, null);
    }

    public static <T> ApiError<T> InternalServer(String message) {
        return new ApiError<>(500, message, null);
    }
}
