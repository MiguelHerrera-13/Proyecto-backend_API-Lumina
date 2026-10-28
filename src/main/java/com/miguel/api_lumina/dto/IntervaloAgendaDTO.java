package com.miguel.api_lumina.dto;

import java.time.LocalDateTime;

public class IntervaloAgendaDTO {

    private LocalDateTime inicio;
    private LocalDateTime fin;
    private String tipo; // "LIBRE" u "OCUPADO"
    private Long idTurno;
    private String detalle;

    public IntervaloAgendaDTO() {
    }

    public IntervaloAgendaDTO(LocalDateTime inicio, LocalDateTime fin, String tipo, Long idTurno, String detalle) {
        this.inicio = inicio;
        this.fin = fin;
        this.tipo = tipo;
        this.idTurno = idTurno;
        this.detalle = detalle;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(Long idTurno) {
        this.idTurno = idTurno;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
}

