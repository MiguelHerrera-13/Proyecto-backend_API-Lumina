package com.miguel.api_lumina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "medicamento")
@SQLDelete(sql = "UPDATE medicamento SET activo = false WHERE id_medicamento=?")
@SQLRestriction("activo=true")
public class MedicamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medicamento")
    private Long idMedicamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tratamiento", nullable = false)
    private TratamientoEntity tratamiento;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "dosis", nullable = false, length = 50)
    private String dosis;

    @Column(name = "frecuencia", nullable = false, length = 100)
    private String frecuencia;

    @Column(name = "activo")
    private Boolean activo = true;

    public MedicamentoEntity() {
    }

    public MedicamentoEntity(Long idMedicamento, TratamientoEntity tratamiento, String nombre, String dosis, String frecuencia, Boolean activo) {
        this.idMedicamento = idMedicamento;
        this.tratamiento = tratamiento;
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

    public TratamientoEntity getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(TratamientoEntity tratamiento) {
        this.tratamiento = tratamiento;
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
