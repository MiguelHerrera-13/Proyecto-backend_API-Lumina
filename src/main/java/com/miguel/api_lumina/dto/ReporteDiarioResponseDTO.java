package com.miguel.api_lumina.dto;

import java.time.LocalDateTime;

public class ReporteDiarioResponseDTO {

    private Long idReporte;
    private Long idTurno;
    private Double temperatura;
    private String presionArterial;
    private String observacion;
    private LocalDateTime fechaRegistro;
    private Boolean activo;

    public ReporteDiarioResponseDTO() {
    }

    public ReporteDiarioResponseDTO(Long idReporte, Long idTurno, Double temperatura, String presionArterial, String observacion, LocalDateTime fechaRegistro, Boolean activo) {
        this.idReporte = idReporte;
        this.idTurno = idTurno;
        this.temperatura = temperatura;
        this.presionArterial = presionArterial;
        this.observacion = observacion;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
    }

    public Long getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Long idReporte) {
        this.idReporte = idReporte;
    }

    public Long getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(Long idTurno) {
        this.idTurno = idTurno;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public String getPresionArterial() {
        return presionArterial;
    }

    public void setPresionArterial(String presionArterial) {
        this.presionArterial = presionArterial;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
