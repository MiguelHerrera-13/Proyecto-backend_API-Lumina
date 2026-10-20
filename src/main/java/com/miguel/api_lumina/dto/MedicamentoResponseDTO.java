package com.miguel.api_lumina.dto;

public class MedicamentoResponseDTO {

    private Long idMedicamento;
    private Long idTratamiento;
    private String nombre;
    private String dosis;
    private String frecuencia;
    private Boolean activo;

    public MedicamentoResponseDTO() {
    }

    public MedicamentoResponseDTO(Long idMedicamento, Long idTratamiento, String nombre, String dosis, String frecuencia, Boolean activo) {
        this.idMedicamento = idMedicamento;
        this.idTratamiento = idTratamiento;
        this.nombre = nombre;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
        this.activo = activo;
    }

    public Long getIdMedicamento() {
        return idMedicamento;
    }

    public void setIdMedicamento(Long idMedicamento) {
        this.idMedicamento = idMedicamento;
    }

    public Long getIdTratamiento() {
        return idTratamiento;
    }

    public void setIdTratamiento(Long idTratamiento) {
        this.idTratamiento = idTratamiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
