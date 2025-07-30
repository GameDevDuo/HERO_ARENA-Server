package com.gamedevduo.heroarena.global.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record ApiError(
    HttpStatus statusCode, String message,
    @JsonInclude(JsonInclude.Include.NON_NULL) Object details
) {}
