package com.miguel.api_lumina.dto;

import jakarta.validation.constraints.*;

public class ReporteDiarioRequestDTO {

    @NotNull(message = "El ID del turno es obligatorio")
    private Long idTurno;

    @NotNull(message = "La temperatura es obligatoria")
    @DecimalMin(value = "30.0", message = "La temperatura no puede ser menor a 30°C")
    @DecimalMax(value = "45.0", message = "La temperatura no puede ser mayor a 45°C")
    private Double temperatura;

    @NotBlank(message = "La presión arterial es obligatoria")
    @Pattern(regexp = "^\\d{2,3}/\\d{2,3}$", message = "La presión arterial debe tener el formato sistólica/diastólica (ej. 120/80)")
    private String presionArterial;

    @NotBlank(message = "La observación no puede estar vacía")
    private String observacion;

    public ReporteDiarioRequestDTO() {
    }

    public ReporteDiarioRequestDTO(Long idTurno, Double temperatura, String presionArterial, String observacion) {
        this.idTurno = idTurno;
        this.temperatura = temperatura;
        this.presionArterial = presionArterial;
        this.observacion = observacion;
    }

    public Long getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(Long idTurno) {
        this.idTurno = idTurno;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public String getPresionArterial() {
        return presionArterial;
    }

    public void setPresionArterial(String presionArterial) {
        this.presionArterial = presionArterial;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
