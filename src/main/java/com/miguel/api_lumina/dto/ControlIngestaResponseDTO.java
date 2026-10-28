package com.miguel.api_lumina.dto;

import java.time.LocalDateTime;

public class ControlIngestaResponseDTO {

    private Long idControlIngesta;
    private Long idReporte;
    private String tipoComida;
    private String descripcionComida;
    private Integer porcentajeConsumido;
    private Integer aguaMl;
    private LocalDateTime fechaHora;
    private String observaciones;
    private Boolean activo;

    public ControlIngestaResponseDTO() {
    }

    public ControlIngestaResponseDTO(Long idControlIngesta, Long idReporte, String tipoComida,
                                     String descripcionComida, Integer porcentajeConsumido,
                                     Integer aguaMl, LocalDateTime fechaHora,
                                     String observaciones, Boolean activo) {
        this.idControlIngesta = idControlIngesta;
        this.idReporte = idReporte;
        this.tipoComida = tipoComida;
        this.descripcionComida = descripcionComida;
        this.porcentajeConsumido = porcentajeConsumido;
        this.aguaMl = aguaMl;
        this.fechaHora = fechaHora;
        this.observaciones = observaciones;
        this.activo = activo;
    }

    public Long getIdControlIngesta() {
        return idControlIngesta;
    }

    public void setIdControlIngesta(Long idControlIngesta) {
        this.idControlIngesta = idControlIngesta;
    }

    public Long getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Long idReporte) {
        this.idReporte = idReporte;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}

