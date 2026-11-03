package com.miguel.api_lumina.dto;

import java.time.LocalDate;

public class PacienteResponseDTO {

    private Long idPaciente;
    private String nombreCompleto;
    private LocalDate fechaNacimiento;
    private String diagnosticoFase;
    private Boolean activo;

    private Long idEmpleadoAsignado;
    private String nombreEmpleadoAsignado;
    private String rolEmpleadoAsignado;

    private Long idFamiliarAsignado;
    private String nombreFamiliarAsignado;
    private String emailFamiliarAsignado;
    private String parentescoFamiliar;

    public PacienteResponseDTO() {
    }

    public PacienteResponseDTO(Long idPaciente, String nombreCompleto, LocalDate fechaNacimiento, String diagnosticoFase, Boolean activo) {
        this.idPaciente = idPaciente;
        this.nombreCompleto = nombreCompleto;
        this.fechaNacimiento = fechaNacimiento;
        this.diagnosticoFase = diagnosticoFase;
        this.activo = activo;
    }

    public PacienteResponseDTO(Long idPaciente, String nombreCompleto, LocalDate fechaNacimiento, String diagnosticoFase, Boolean activo,
                               Long idEmpleadoAsignado, String nombreEmpleadoAsignado, String rolEmpleadoAsignado,
                               Long idFamiliarAsignado, String nombreFamiliarAsignado, String emailFamiliarAsignado, String parentescoFamiliar) {
        this.idPaciente = idPaciente;
        this.nombreCompleto = nombreCompleto;
        this.fechaNacimiento = fechaNacimiento;
        this.diagnosticoFase = diagnosticoFase;
        this.activo = activo;
        this.idEmpleadoAsignado = idEmpleadoAsignado;
        this.nombreEmpleadoAsignado = nombreEmpleadoAsignado;
        this.rolEmpleadoAsignado = rolEmpleadoAsignado;
        this.idFamiliarAsignado = idFamiliarAsignado;
        this.nombreFamiliarAsignado = nombreFamiliarAsignado;
        this.emailFamiliarAsignado = emailFamiliarAsignado;
        this.parentescoFamiliar = parentescoFamiliar;
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
