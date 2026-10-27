package com.miguel.api_lumina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "signos_vitales")
@SQLDelete(sql = "UPDATE signos_vitales SET activo = false WHERE id_signo_vital=?")
@SQLRestriction("activo=true")
public class SignosVitalesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_signo_vital")
    private Long idSignoVital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private PacienteEntity paciente;

    @Column(name = "temperatura", nullable = false)
    private Double temperatura;

    @Column(name = "presion_arterial", nullable = false, length = 20)
    private String presionArterial;

    @Column(name = "frecuencia_cardiaca", nullable = false)
    private Integer frecuenciaCardiaca;

    @Column(name = "glucemia", nullable = false)
    private Double glucemia;

    @Column(name = "autor", nullable = false, updatable = false, length = 100)
    private String autor;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "activo")
    private Boolean activo = true;

    public SignosVitalesEntity() {
    }

    public SignosVitalesEntity(PacienteEntity paciente, Double temperatura, String presionArterial,
                               Integer frecuenciaCardiaca, Double glucemia, String autor,
                               LocalDateTime fechaHora, String observaciones) {
        this.paciente = paciente;
        this.temperatura = temperatura;
        this.presionArterial = presionArterial;
        this.frecuenciaCardiaca = frecuenciaCardiaca;
        this.glucemia = glucemia;
        this.autor = autor;
        this.fechaHora = fechaHora;
        this.observaciones = observaciones;
        this.activo = true;
    }

    public Long getIdSignoVital() {
        return idSignoVital;
    }

    public void setIdSignoVital(Long idSignoVital) {
        this.idSignoVital = idSignoVital;
    }

    public PacienteEntity getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteEntity paciente) {
        this.paciente = paciente;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
