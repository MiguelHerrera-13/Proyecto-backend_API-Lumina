package com.miguel.api_lumina.dto;

import com.miguel.api_lumina.entity.TipoAsignacionProfesional;

import java.time.LocalDateTime;

public class AsignacionProfesionalResponseDTO {

    private Long idAsignacion;
    private Long idPaciente;
    private Long idUsuario;
    private String nombreUsuario;
    private TipoAsignacionProfesional tipoAsignacion;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    private Boolean activa;

    public AsignacionProfesionalResponseDTO() {
    }

    public AsignacionProfesionalResponseDTO(
            Long idAsignacion,
            Long idPaciente,
            Long idUsuario,
            String nombreUsuario,
            TipoAsignacionProfesional tipoAsignacion,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Boolean activa) {

        this.idAsignacion = idAsignacion;
        this.idPaciente = idPaciente;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.tipoAsignacion = tipoAsignacion;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.activa = activa;
    }

    public Long getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(Long idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    public Long getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Long idPaciente) {
        this.idPaciente = idPaciente;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public TipoAsignacionProfesional getTipoAsignacion() {
        return tipoAsignacion;
    }

    public void setTipoAsignacion(TipoAsignacionProfesional tipoAsignacion) {
        this.tipoAsignacion = tipoAsignacion;
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

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
}