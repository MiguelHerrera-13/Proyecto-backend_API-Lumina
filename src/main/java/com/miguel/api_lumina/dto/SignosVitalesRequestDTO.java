package com.miguel.api_lumina.dto;

import jakarta.validation.constraints.*;

public class SignosVitalesRequestDTO {

    @NotNull(message = "La temperatura es obligatoria")
    @DecimalMin(value = "34.0", message = "La temperatura debe ser de al menos 34.0 °C")
    @DecimalMax(value = "43.0", message = "La temperatura no puede superar los 43.0 °C")
    private Double temperatura;

    @NotBlank(message = "La presión arterial es obligatoria")
    @Pattern(regexp = "^\\d{2,3}/\\d{2,3}$", message = "El formato de presión arterial debe ser sistólica/diastólica (ej. 120/80)")
    private String presionArterial;

    @NotNull(message = "La frecuencia cardíaca es obligatoria")
    @Min(value = 30, message = "La frecuencia cardíaca debe ser de al menos 30 lpm")
    @Max(value = 250, message = "La frecuencia cardíaca no puede superar los 250 lpm")
    private Integer frecuenciaCardiaca;

    @NotNull(message = "El nivel de glucemia es obligatorio")
    @DecimalMin(value = "20.0", message = "El nivel de glucemia debe ser de al menos 20.0 mg/dL")
    @DecimalMax(value = "600.0", message = "El nivel de glucemia no puede superar los 600.0 mg/dL")
    private Double glucemia;

    private String autor;

    private String observaciones;

    public SignosVitalesRequestDTO() {
    }

    public SignosVitalesRequestDTO(Double temperatura, String presionArterial, Integer frecuenciaCardiaca,
                                  Double glucemia, String autor, String observaciones) {
        this.temperatura = temperatura;
        this.presionArterial = presionArterial;
        this.frecuenciaCardiaca = frecuenciaCardiaca;
        this.glucemia = glucemia;
        this.autor = autor;
        this.observaciones = observaciones;
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

    public Integer getFrecuenciaCardiaca() {
        return frecuenciaCardiaca;
    }

    public void setFrecuenciaCardiaca(Integer frecuenciaCardiaca) {
        this.frecuenciaCardiaca = frecuenciaCardiaca;
    }

    public Double getGlucemia() {
        return glucemia;
    }

    public void setGlucemia(Double glucemia) {
        this.glucemia = glucemia;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
