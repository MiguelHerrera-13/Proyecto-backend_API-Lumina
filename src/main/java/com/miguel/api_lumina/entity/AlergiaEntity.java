package com.miguel.api_lumina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "alergia")
@SQLDelete(sql = "UPDATE alergia SET activo = false WHERE id_alergia=?")
@SQLRestriction("activo=true")
public class AlergiaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alergia")
    private Long idAlergia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private PacienteEntity paciente;

    @Column(name = "tipo", nullable = false, length = 100)
    private String tipo;

    @Column(name = "severidad", nullable = false, length = 20)
    private String severidad;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "activo")
    private Boolean activo = true;

    public AlergiaEntity() {
    }

    public AlergiaEntity(PacienteEntity paciente, String tipo, String severidad, String descripcion) {
        this.paciente = paciente;
        this.tipo = tipo;
        this.severidad = severidad;
        this.descripcion = descripcion;
        this.activo = true;
    }

    public Long getIdAlergia() {
        return idAlergia;
    }

    public void setIdAlergia(Long idAlergia) {
        this.idAlergia = idAlergia;
    }

    public PacienteEntity getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteEntity paciente) {
        this.paciente = paciente;
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
