package com.miguel.api_lumina.dto;

import jakarta.validation.constraints.NotBlank;

public class ResetPasswordRequestDTO {

    @NotBlank(message = "El token es obligatorio")
    private String token;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String nuevaPassword;

    public ResetPasswordRequestDTO() {
    }

    public ResetPasswordRequestDTO(String token, String nuevaPassword) {
        this.token = token;
        this.nuevaPassword = nuevaPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNuevaPassword() {
        return nuevaPassword;
    }

    public void setNuevaPassword(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }

    public void setPassword(String password) {
        if (this.nuevaPassword == null || this.nuevaPassword.isBlank()) {
            this.nuevaPassword = password;
        }
    }

    public void setNewPassword(String newPassword) {
        if (this.nuevaPassword == null || this.nuevaPassword.isBlank()) {
            this.nuevaPassword = newPassword;
        }
    }
}
