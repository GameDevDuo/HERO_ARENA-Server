package com.gamedevduo.heroarena.global.security.jwt.dto;

public record UserCredential(
    Long id,
    String email,
    String encodedPassword
) {}