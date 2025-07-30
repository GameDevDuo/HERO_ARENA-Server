package com.gamedevduo.heroarena.global.exception.dto;

import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record ErrorDetail(HttpStatus statusCode, String message) {}