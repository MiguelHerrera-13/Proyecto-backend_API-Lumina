package com.miguel.api_lumina.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ControlIngestaRequestDTO {

    @NotBlank(message = "El tipo de comida es obligatorio")
    private String tipoComida;

    private String descripcionComida;

    @NotNull(message = "El porcentaje consumido es obligatorio")
    @Min(value = 0, message = "El porcentaje consumido no puede ser menor a 0")
    @Max(value = 100, message = "El porcentaje consumido no puede ser mayor a 100")
    private Integer porcentajeConsumido;

    @NotNull(message = "La cantidad de agua en mililitros es obligatoria")
    @Min(value = 0, message = "Los mililitros de agua no pueden ser negativos")
    private Integer aguaMl;

    private LocalDateTime fechaHora;

    private String observaciones;

    public ControlIngestaRequestDTO() {
    }

    public ControlIngestaRequestDTO(String tipoComida, String descripcionComida,
                                    Integer porcentajeConsumido, Integer aguaMl,
                                    LocalDateTime fechaHora, String observaciones) {
        this.tipoComida = tipoComida;
        this.descripcionComida = descripcionComida;
        this.porcentajeConsumido = porcentajeConsumido;
        this.aguaMl = aguaMl;
        this.fechaHora = fechaHora;
        this.observaciones = observaciones;
    }

    public String getTipoComida() {
        return tipoComida;
    }

    public void setTipoComida(String tipoComida) {
        this.tipoComida = tipoComida;
    }

    public String getDescripcionComida() {
        return descripcionComida;
    }

    public void setDescripcionComida(String descripcionComida) {
        this.descripcionComida = descripcionComida;
    }

    public Integer getPorcentajeConsumido() {
        return porcentajeConsumido;
    }

    public void setPorcentajeConsumido(Integer porcentajeConsumido) {
        this.porcentajeConsumido = porcentajeConsumido;
    }

    public Integer getAguaMl() {
        return aguaMl;
    }

    public void setAguaMl(Integer aguaMl) {
        this.aguaMl = aguaMl;
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

