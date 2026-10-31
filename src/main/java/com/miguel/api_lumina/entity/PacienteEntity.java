package com.miguel.api_lumina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "paciente")
@SQLDelete(sql = "UPDATE paciente SET activo = false WHERE id_paciente=?")
@SQLRestriction("activo=true")
public class PacienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Long idPaciente;

    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "diagnostico_fase")
    private String diagnosticoFase;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "motivo_baja", length = 255)
    private String motivoBaja;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    @Column(name = "usuario_baja", length = 100)
    private String usuarioBaja;

    public PacienteEntity() {
    }

    public PacienteEntity(Long idPaciente, String nombreCompleto, LocalDate fechaNacimiento, String diagnosticoFase, Boolean activo) {
        this.idPaciente = idPaciente;
        this.nombreCompleto = nombreCompleto;
        this.fechaNacimiento = fechaNacimiento;
        this.diagnosticoFase = diagnosticoFase;
        this.activo = activo;
    }

    public Long getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Long idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getDiagnosticoFase() {
        return diagnosticoFase;
    }

    public void setDiagnosticoFase(String diagnosticoFase) {
        this.diagnosticoFase = diagnosticoFase;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getMotivoBaja() {
        return motivoBaja;
    }

    public void setMotivoBaja(String motivoBaja) {
        this.motivoBaja = motivoBaja;
    }

    public LocalDateTime getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(LocalDateTime fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public String getUsuarioBaja() {
        return usuarioBaja;
    }

    public void setUsuarioBaja(String usuarioBaja) {
        this.usuarioBaja = usuarioBaja;
    }
}
