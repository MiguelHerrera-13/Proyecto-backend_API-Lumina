package com.miguel.api_lumina.dto;

import java.time.LocalDate;

public class PacienteResponseDTO {

    private Long idPaciente;
    private String nombreCompleto;
    private LocalDate fechaNacimiento;
    private String diagnosticoFase;
    private Boolean activo;

    public PacienteResponseDTO() {
    }

    public PacienteResponseDTO(Long idPaciente, String nombreCompleto, LocalDate fechaNacimiento, String diagnosticoFase, Boolean activo) {
        this.idPaciente = idPaciente;
        this.nombreCompleto = nombreCompleto;
        this.fechaNacimiento = fechaNacimiento;
        this.diagnosticoFase = diagnosticoFase;
        this.activo = activo;
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

}
