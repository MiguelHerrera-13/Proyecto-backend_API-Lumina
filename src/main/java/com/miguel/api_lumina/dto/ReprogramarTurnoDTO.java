package com.miguel.api_lumina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ReprogramarTurnoDTO {

    @NotNull(message = "La nueva fecha y hora de inicio es obligatoria")
    private LocalDateTime nuevaFechaHoraInicio;

    @NotNull(message = "La nueva fecha y hora de fin es obligatoria")
    private LocalDateTime nuevaFechaHoraFin;

    @NotBlank(message = "El motivo de reprogramación es obligatorio")
    @Size(min = 10, message = "El motivo de reprogramación debe tener al menos 10 caracteres")
    private String motivo;

    public ReprogramarTurnoDTO() {
    }

    public ReprogramarTurnoDTO(LocalDateTime nuevaFechaHoraInicio, LocalDateTime nuevaFechaHoraFin, String motivo) {
        this.nuevaFechaHoraInicio = nuevaFechaHoraInicio;
        this.nuevaFechaHoraFin = nuevaFechaHoraFin;
        this.motivo = motivo;
    }

    public LocalDateTime getNuevaFechaHoraInicio() {
        return nuevaFechaHoraInicio;
    }

    public void setNuevaFechaHoraInicio(LocalDateTime nuevaFechaHoraInicio) {
        this.nuevaFechaHoraInicio = nuevaFechaHoraInicio;
    }

    public LocalDateTime getNuevaFechaHoraFin() {
        return nuevaFechaHoraFin;
    }

    public void setNuevaFechaHoraFin(LocalDateTime nuevaFechaHoraFin) {
        this.nuevaFechaHoraFin = nuevaFechaHoraFin;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}

