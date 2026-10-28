package com.miguel.api_lumina.dto;

import java.time.LocalDateTime;

public class TurnoFiltroDTO {

    private Long pacienteId;
    private Long profesionalId;
    private Long medicoId;
    private Long cuidadorId;
    private String estado;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;

    public TurnoFiltroDTO() {
    }

    public TurnoFiltroDTO(Long pacienteId, Long profesionalId, Long medicoId, Long cuidadorId, String estado, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        this.pacienteId = pacienteId;
        this.profesionalId = profesionalId;
        this.medicoId = medicoId;
        this.cuidadorId = cuidadorId;
        this.estado = estado;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Long getProfesionalId() {
        return profesionalId;
    }

    public void setProfesionalId(Long profesionalId) {
        this.profesionalId = profesionalId;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public void setMedicoId(Long medicoId) {
        this.medicoId = medicoId;
    }

    public Long getCuidadorId() {
        return cuidadorId;
    }

    public void setCuidadorId(Long cuidadorId) {
        this.cuidadorId = cuidadorId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDateTime fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDateTime getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDateTime fechaHasta) {
        this.fechaHasta = fechaHasta;
    }
}

