package com.miguel.api_lumina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TurnoRecurrenteRequestDTO {

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long idPaciente;

    @NotNull(message = "El ID del cuidador es obligatorio")
    private Long idCuidador;

    @NotNull(message = "El ID del médico es obligatorio")
    private Long idMedico;

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    private LocalDateTime fechaHoraInicio;

    @NotNull(message = "La fecha y hora de fin es obligatoria")
    private LocalDateTime fechaHoraFin;

    @NotBlank(message = "El patrón de recurrencia es obligatorio")
    private String patronRecurrencia;

    @NotNull(message = "La fecha límite de recurrencia es obligatoria")
    private LocalDate fechaLimite;

    private String estado;

    public TurnoRecurrenteRequestDTO() {
    }

    public TurnoRecurrenteRequestDTO(Long idPaciente, Long idCuidador, Long idMedico,
                                     LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin,
                                     String patronRecurrencia, LocalDate fechaLimite, String estado) {
        this.idPaciente = idPaciente;
        this.idCuidador = idCuidador;
        this.idMedico = idMedico;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.patronRecurrencia = patronRecurrencia;
        this.fechaLimite = fechaLimite;
        this.estado = estado;
    }

    public Long getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Long idPaciente) {
        this.idPaciente = idPaciente;
    }

    public Long getIdCuidador() {
        return idCuidador;
    }

    public void setIdCuidador(Long idCuidador) {
        this.idCuidador = idCuidador;
    }

    public Long getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(Long idMedico) {
        this.idMedico = idMedico;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public String getPatronRecurrencia() {
        return patronRecurrencia;
    }

    public void setPatronRecurrencia(String patronRecurrencia) {
        this.patronRecurrencia = patronRecurrencia;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
