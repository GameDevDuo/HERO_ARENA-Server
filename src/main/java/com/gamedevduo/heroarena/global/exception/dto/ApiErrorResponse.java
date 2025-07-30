package com.gamedevduo.heroarena.global.exception.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiErrorResponse {
    private final boolean success = false;
    private final ErrorDetail error;
    private final String timestamp = java.time.Instant.now().toString();

    public ApiErrorResponse(HttpStatus statusCode, String message) {
        this.error = new ErrorDetail(statusCode, message);
    }
}