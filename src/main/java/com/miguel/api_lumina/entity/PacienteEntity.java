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

    @Column(name = "id_empleado_asignado")
    private Long idEmpleadoAsignado;

    @Column(name = "nombre_empleado_asignado", length = 150)
    private String nombreEmpleadoAsignado;

    @Column(name = "rol_empleado_asignado", length = 50)
    private String rolEmpleadoAsignado;

    @Column(name = "id_familiar_asignado")
    private Long idFamiliarAsignado;

    @Column(name = "nombre_familiar_asignado", length = 150)
    private String nombreFamiliarAsignado;

    @Column(name = "email_familiar_asignado", length = 150)
    private String emailFamiliarAsignado;

    @Column(name = "parentesco_familiar", length = 50)
    private String parentescoFamiliar;

    @Column(name = "activo")
    private Boolean activo = true;

    public PacienteEntity() {
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

    public Long getIdEmpleadoAsignado() {
        return idEmpleadoAsignado;
    }

    public void setIdEmpleadoAsignado(Long idEmpleadoAsignado) {
        this.idEmpleadoAsignado = idEmpleadoAsignado;
    }

    public String getNombreEmpleadoAsignado() {
        return nombreEmpleadoAsignado;
    }

    public void setNombreEmpleadoAsignado(String nombreEmpleadoAsignado) {
        this.nombreEmpleadoAsignado = nombreEmpleadoAsignado;
    }

    public String getRolEmpleadoAsignado() {
        return rolEmpleadoAsignado;
    }

    public void setRolEmpleadoAsignado(String rolEmpleadoAsignado) {
        this.rolEmpleadoAsignado = rolEmpleadoAsignado;
    }

    public Long getIdFamiliarAsignado() {
        return idFamiliarAsignado;
    }

    public void setIdFamiliarAsignado(Long idFamiliarAsignado) {
        this.idFamiliarAsignado = idFamiliarAsignado;
    }

    public String getNombreFamiliarAsignado() {
        return nombreFamiliarAsignado;
    }

    public void setNombreFamiliarAsignado(String nombreFamiliarAsignado) {
        this.nombreFamiliarAsignado = nombreFamiliarAsignado;
    }

    public String getEmailFamiliarAsignado() {
        return emailFamiliarAsignado;
    }

    public void setEmailFamiliarAsignado(String emailFamiliarAsignado) {
        this.emailFamiliarAsignado = emailFamiliarAsignado;
    }

    public String getParentescoFamiliar() {
        return parentescoFamiliar;
    }

    public void setParentescoFamiliar(String parentescoFamiliar) {
        this.parentescoFamiliar = parentescoFamiliar;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
