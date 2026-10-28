package io.github.timur0812.pokerodds.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Bad input becomes 400 {"error": ...}.
@RestControllerAdvice
public class ApiExceptionHandler {

    public record ApiError(String error) {
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequest(IllegalArgumentException e) {
        return new ApiError(e.getMessage());
    }
}
