package com.gamedevduo.heroarena.global.exception;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FilterExceptionResponse {
    boolean success;
    Map<String, Object> error;
    String timestamp;
}