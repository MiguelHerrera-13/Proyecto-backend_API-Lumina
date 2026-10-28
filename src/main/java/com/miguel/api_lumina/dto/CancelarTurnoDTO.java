package com.miguel.api_lumina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CancelarTurnoDTO {

    @NotBlank(message = "El motivo de cancelación es obligatorio")
    @Size(min = 10, message = "El motivo de cancelación debe tener al menos 10 caracteres")
    private String motivo;

    public CancelarTurnoDTO() {
    }

    public CancelarTurnoDTO(String motivo) {
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}

