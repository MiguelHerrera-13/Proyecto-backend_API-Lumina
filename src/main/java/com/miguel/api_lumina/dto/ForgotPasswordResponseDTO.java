package com.miguel.api_lumina.dto;

public class ForgotPasswordResponseDTO {

    private String mensaje;

    public ForgotPasswordResponseDTO() {
    }

    public ForgotPasswordResponseDTO(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
