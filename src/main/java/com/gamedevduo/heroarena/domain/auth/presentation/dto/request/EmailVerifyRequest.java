package com.gamedevduo.heroarena.domain.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerifyRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String code;
}
