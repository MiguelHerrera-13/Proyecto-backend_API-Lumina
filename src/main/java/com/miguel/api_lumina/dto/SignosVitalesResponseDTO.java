package com.miguel.api_lumina.dto;

import java.time.LocalDateTime;

public class SignosVitalesResponseDTO {

    private Long idSignoVital;
    private Long idPaciente;
    private Double temperatura;
    private String presionArterial;
    private Integer frecuenciaCardiaca;
    private Double glucemia;
    private String autor;
    private LocalDateTime fechaHora;
    private String observaciones;

    public SignosVitalesResponseDTO() {
    }

    public SignosVitalesResponseDTO(Long idSignoVital, Long idPaciente, Double temperatura,
                                   String presionArterial, Integer frecuenciaCardiaca,
                                   Double glucemia, String autor, LocalDateTime fechaHora,
                                   String observaciones) {
        this.idSignoVital = idSignoVital;
        this.idPaciente = idPaciente;
        this.temperatura = temperatura;
        this.presionArterial = presionArterial;
        this.frecuenciaCardiaca = frecuenciaCardiaca;
        this.glucemia = glucemia;
        this.autor = autor;
        this.fechaHora = fechaHora;
        this.observaciones = observaciones;
    }

    public Long getIdSignoVital() {
        return idSignoVital;
    }

    public void setIdSignoVital(Long idSignoVital) {
        this.idSignoVital = idSignoVital;
    }

    public Long getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Long idPaciente) {
        this.idPaciente = idPaciente;
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

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
