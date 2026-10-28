package com.miguel.api_lumina.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequestDTO {

    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;

    public RefreshTokenRequestDTO() {
    }

    public RefreshTokenRequestDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Compatibilidad en caso de enviar {"token": "..."}
    public void setToken(String token) {
        if (this.refreshToken == null || this.refreshToken.isBlank()) {
            this.refreshToken = token;
        }
    }
}
