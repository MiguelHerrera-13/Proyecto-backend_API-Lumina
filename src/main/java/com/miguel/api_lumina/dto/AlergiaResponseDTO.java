package com.miguel.api_lumina.dto;

public class AlergiaResponseDTO {

    private Long idAlergia;
    private Long idPaciente;
    private String tipo;
    private String severidad;
    private String descripcion;
    private Boolean activo;

    public AlergiaResponseDTO() {
    }

    public AlergiaResponseDTO(Long idAlergia, Long idPaciente, String tipo, String severidad, String descripcion, Boolean activo) {
        this.idAlergia = idAlergia;
        this.idPaciente = idPaciente;
        this.tipo = tipo;
        this.severidad = severidad;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public Long getIdAlergia() {
        return idAlergia;
    }

    public void setIdAlergia(Long idAlergia) {
        this.idAlergia = idAlergia;
    }

    public Long getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Long idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
