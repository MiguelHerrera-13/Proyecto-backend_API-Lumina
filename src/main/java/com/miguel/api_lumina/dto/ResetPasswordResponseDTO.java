package com.miguel.api_lumina.dto;

public class ResetPasswordResponseDTO {

    private String mensaje;

    public ResetPasswordResponseDTO() {
    }

    public ResetPasswordResponseDTO(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
