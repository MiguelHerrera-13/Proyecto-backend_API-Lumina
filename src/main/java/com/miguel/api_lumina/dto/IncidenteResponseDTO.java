package com.miguel.api_lumina.dto;

import java.time.LocalDateTime;

public class IncidenteResponseDTO {

    private Long idIncidente;
    private Long idReporte;
    private String tipoIncidente;
    private String descripcion;
    private String severidad;
    private LocalDateTime fechaHora;
    private Boolean alertaGenerada;
    private Boolean activo;

    public IncidenteResponseDTO() {
    }

    public IncidenteResponseDTO(Long idIncidente, Long idReporte, String tipoIncidente,
                                String descripcion, String severidad, LocalDateTime fechaHora,
                                Boolean alertaGenerada, Boolean activo) {
        this.idIncidente = idIncidente;
        this.idReporte = idReporte;
        this.tipoIncidente = tipoIncidente;
        this.descripcion = descripcion;
        this.severidad = severidad;
        this.fechaHora = fechaHora;
        this.alertaGenerada = alertaGenerada;
        this.activo = activo;
    }

    public Long getIdIncidente() {
        return idIncidente;
    }

    public void setIdIncidente(Long idIncidente) {
        this.idIncidente = idIncidente;
    }

    public Long getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Long idReporte) {
        this.idReporte = idReporte;
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

    public Boolean getAlertaGenerada() {
        return alertaGenerada;
    }

    public void setAlertaGenerada(Boolean alertaGenerada) {
        this.alertaGenerada = alertaGenerada;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}

