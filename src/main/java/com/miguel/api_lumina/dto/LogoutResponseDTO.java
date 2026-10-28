package com.miguel.api_lumina.dto;

public class LogoutResponseDTO {

    private String mensaje;

    public LogoutResponseDTO() {
    }

    public LogoutResponseDTO(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
