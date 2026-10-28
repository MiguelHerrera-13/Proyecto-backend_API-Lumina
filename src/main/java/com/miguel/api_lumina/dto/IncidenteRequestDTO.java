package com.miguel.api_lumina.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class IncidenteRequestDTO {

    @NotBlank(message = "El tipo de incidente es obligatorio")
    private String tipoIncidente;

    @NotBlank(message = "La descripción del incidente es obligatoria")
    private String descripcion;

    @NotBlank(message = "La severidad es obligatoria")
    private String severidad;

    private LocalDateTime fechaHora;

    public IncidenteRequestDTO() {
    }

    public IncidenteRequestDTO(String tipoIncidente, String descripcion, String severidad, LocalDateTime fechaHora) {
        this.tipoIncidente = tipoIncidente;
        this.descripcion = descripcion;
        this.severidad = severidad;
        this.fechaHora = fechaHora;
    }

    public String getTipoIncidente() {
        return tipoIncidente;
    }

    public void setTipoIncidente(String tipoIncidente) {
        this.tipoIncidente = tipoIncidente;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}
