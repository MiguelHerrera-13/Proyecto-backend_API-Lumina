package com.miguel.api_lumina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AlergiaRequestDTO {

    @NotBlank(message = "El tipo de alergia es obligatorio")
    private String tipo;

    @NotBlank(message = "La severidad es obligatoria")
    @Pattern(regexp = "^(?i)(LEVE|MODERADA|GRAVE)$", message = "La severidad debe ser LEVE, MODERADA o GRAVE")
    private String severidad;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    public AlergiaRequestDTO() {
    }

    public AlergiaRequestDTO(String tipo, String severidad, String descripcion) {
        this.tipo = tipo;
        this.severidad = severidad;
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
