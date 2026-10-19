package com.miguel.api_lumina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MedicamentoRequestDTO {

    @NotNull(message = "El ID del tratamiento es obligatorio")
    private Long idTratamiento;

    @NotBlank(message = "El nombre del medicamento es obligatorio")
    private String nombre;

    @NotBlank(message = "La dosis del medicamento es obligatoria")
    private String dosis;

    @NotBlank(message = "La frecuencia del medicamento es obligatoria")
    private String frecuencia;

    public MedicamentoRequestDTO() {
    }

    public MedicamentoRequestDTO(Long idTratamiento, String nombre, String dosis, String frecuencia) {
        this.idTratamiento = idTratamiento;
        this.nombre = nombre;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
    }

    public Long getIdTratamiento() {
        return idTratamiento;
    }

    public void setIdTratamiento(Long idTratamiento) {
        this.idTratamiento = idTratamiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }
}
