package com.miguel.api_lumina.dto;

import java.time.LocalDate;

public class PacienteRequestDTO {

    private String nombreCompleto;
    private LocalDate fechaNacimiento;
    private String diagnosticoFase;

    private Long idEmpleadoAsignado;
    private String nombreEmpleadoAsignado;
    private String rolEmpleadoAsignado;

    private Long idFamiliarAsignado;
    private String nombreFamiliarAsignado;
    private String emailFamiliarAsignado;
    private String parentescoFamiliar;

    public PacienteRequestDTO() {
    }

    public PacienteRequestDTO(String nombreCompleto, LocalDate fechaNacimiento, String diagnosticoFase) {
        this.nombreCompleto = nombreCompleto;
        this.fechaNacimiento = fechaNacimiento;
        this.diagnosticoFase = diagnosticoFase;
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

    public Long getIdEmpleadoAsignado() {
        return idEmpleadoAsignado;
    }

    public void setIdEmpleadoAsignado(Long idEmpleadoAsignado) {
        this.idEmpleadoAsignado = idEmpleadoAsignado;
    }

    public String getNombreEmpleadoAsignado() {
        return nombreEmpleadoAsignado;
    }

    public void setNombreEmpleadoAsignado(String nombreEmpleadoAsignado) {
        this.nombreEmpleadoAsignado = nombreEmpleadoAsignado;
    }

    public String getRolEmpleadoAsignado() {
        return rolEmpleadoAsignado;
    }

    public void setRolEmpleadoAsignado(String rolEmpleadoAsignado) {
        this.rolEmpleadoAsignado = rolEmpleadoAsignado;
    }

    public Long getIdFamiliarAsignado() {
        return idFamiliarAsignado;
    }

    public void setIdFamiliarAsignado(Long idFamiliarAsignado) {
        this.idFamiliarAsignado = idFamiliarAsignado;
    }

    public String getNombreFamiliarAsignado() {
        return nombreFamiliarAsignado;
    }

    public void setNombreFamiliarAsignado(String nombreFamiliarAsignado) {
        this.nombreFamiliarAsignado = nombreFamiliarAsignado;
    }

    public String getEmailFamiliarAsignado() {
        return emailFamiliarAsignado;
    }

    public void setEmailFamiliarAsignado(String emailFamiliarAsignado) {
        this.emailFamiliarAsignado = emailFamiliarAsignado;
    }

    public String getParentescoFamiliar() {
        return parentescoFamiliar;
    }

    public void setParentescoFamiliar(String parentescoFamiliar) {
        this.parentescoFamiliar = parentescoFamiliar;
    }
}
