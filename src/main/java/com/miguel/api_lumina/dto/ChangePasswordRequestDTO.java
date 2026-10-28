package com.miguel.api_lumina.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ChangePasswordRequestDTO {

    @NotBlank(message = "La contraseña actual no puede estar vacía")
    @JsonAlias({"currentPassword", "actualPassword"})
    private String passwordActual;

    @NotBlank(message = "La nueva contraseña no puede estar vacía")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "La nueva contraseña debe tener al menos 8 caracteres, incluir al menos una letra mayúscula y un número"
    )
    @JsonAlias({"newPassword", "password"})
    private String nuevaPassword;

    @NotBlank(message = "La confirmación de la contraseña no puede estar vacía")
    @JsonAlias({"confirmPassword", "confirmacionPassword"})
    private String confirmarPassword;

    public ChangePasswordRequestDTO() {
    }

    public ChangePasswordRequestDTO(String passwordActual, String nuevaPassword, String confirmarPassword) {
        this.passwordActual = passwordActual;
        this.nuevaPassword = nuevaPassword;
        this.confirmarPassword = confirmarPassword;
    }

    public String getPasswordActual() {
        return passwordActual;
    }

    public void setPasswordActual(String passwordActual) {
        this.passwordActual = passwordActual;
    }

    public String getNuevaPassword() {
        return nuevaPassword;
    }

    public void setNuevaPassword(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }

    public String getConfirmarPassword() {
        return confirmarPassword;
    }

    public void setConfirmarPassword(String confirmarPassword) {
        this.confirmarPassword = confirmarPassword;
    }
}
