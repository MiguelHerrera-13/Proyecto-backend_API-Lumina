package com.miguel.api_lumina.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PacienteInactivoResponseDTO {

    private Long idPaciente;
    private String nombreCompleto;
    private LocalDate fechaNacimiento;
    private String diagnosticoFase;
    private Boolean activo;
    private String motivoBaja;
    private LocalDateTime fechaBaja;
    private String usuarioBaja;

    public PacienteInactivoResponseDTO() {
    }

    public PacienteInactivoResponseDTO(Long idPaciente, String nombreCompleto, LocalDate fechaNacimiento,
                                      String diagnosticoFase, Boolean activo, String motivoBaja,
                                      LocalDateTime fechaBaja, String usuarioBaja) {
        this.idPaciente = idPaciente;
        this.nombreCompleto = nombreCompleto;
        this.fechaNacimiento = fechaNacimiento;
        this.diagnosticoFase = diagnosticoFase;
        this.activo = activo;
        this.motivoBaja = motivoBaja;
        this.fechaBaja = fechaBaja;
        this.usuarioBaja = usuarioBaja;
    }

    public Long getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Long idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getDiagnosticoFase() {
        return diagnosticoFase;
    }

    public void setDiagnosticoFase(String diagnosticoFase) {
        this.diagnosticoFase = diagnosticoFase;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getMotivoBaja() {
        return motivoBaja;
    }

    public void setMotivoBaja(String motivoBaja) {
        this.motivoBaja = motivoBaja;
    }

    public LocalDateTime getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(LocalDateTime fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public String getUsuarioBaja() {
        return usuarioBaja;
    }

    public void setUsuarioBaja(String usuarioBaja) {
        this.usuarioBaja = usuarioBaja;
    }
}
