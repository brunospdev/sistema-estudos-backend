package com.planejamais.dto;

import com.planejamais.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetConfirmRequest {
    @NotBlank
    private String token;

    @NotBlank
    @StrongPassword
    private String novaSenha;
}
