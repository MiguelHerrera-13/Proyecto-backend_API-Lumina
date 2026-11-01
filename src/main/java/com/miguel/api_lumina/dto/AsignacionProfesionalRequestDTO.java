package com.miguel.api_lumina.dto;

import jakarta.validation.constraints.NotNull;

public class AsignacionProfesionalRequestDTO {

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long idUsuario;

    public AsignacionProfesionalRequestDTO() {
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
}