package com.miguel.api_lumina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public class PacienteRequestDTO {

    private String nombreCompleto;

    private LocalDate fechaNacimiento;
    
    private String diagnosticoFase;

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

}
