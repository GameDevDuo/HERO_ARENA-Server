package com.gamedevduo.heroarena.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamedevduo.heroarena.global.exception.FilterExceptionResponse;
import com.gamedevduo.heroarena.global.exception.HttpException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (HttpException e) {
            response.setStatus(e.getStatusCode().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> error = Map.of(
                    "statusCode", e.getStatusCode(),
                    "message", e.getMessage()
            );

            FilterExceptionResponse errorResponse = new FilterExceptionResponse(
                    false,
                    error,
                    Instant.now().toString()
            );

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}
