package com.miguel.api_lumina.dto;

public class ChangePasswordResponseDTO {

    private String mensaje;

    public ChangePasswordResponseDTO() {
    }

    public ChangePasswordResponseDTO(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
