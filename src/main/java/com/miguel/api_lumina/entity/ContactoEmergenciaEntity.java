package com.miguel.api_lumina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "contacto_emergencia")
@SQLDelete(sql = "UPDATE contacto_emergencia SET activo = false WHERE id_contacto_emergencia=?")
@SQLRestriction("activo=true")
public class ContactoEmergenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contacto_emergencia")
    private Long idContactoEmergencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private PacienteEntity paciente;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "relacion", nullable = false, length = 50)
    private String relacion;

    @Column(name = "telefono", nullable = false, length = 30)
    private String telefono;

    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal = false;

    @Column(name = "activo")
    private Boolean activo = true;

    public ContactoEmergenciaEntity() {
    }

    public ContactoEmergenciaEntity(PacienteEntity paciente, String nombre, String relacion, String telefono, Boolean esPrincipal) {
        this.paciente = paciente;
        this.nombre = nombre;
        this.relacion = relacion;
        this.telefono = telefono;
        this.esPrincipal = (esPrincipal != null) ? esPrincipal : false;
        this.activo = true;
    }

    public Long getIdContactoEmergencia() {
        return idContactoEmergencia;
    }

    public void setIdContactoEmergencia(Long idContactoEmergencia) {
        this.idContactoEmergencia = idContactoEmergencia;
    }

    public PacienteEntity getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteEntity paciente) {
        this.paciente = paciente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRelacion() {
        return relacion;
    }

    public void setRelacion(String relacion) {
        this.relacion = relacion;
    }

    public String getParentesco() {
        return relacion;
    }

    public void setParentesco(String parentesco) {
        this.relacion = parentesco;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Boolean getEsPrincipal() {
        return esPrincipal;
    }

    public void setEsPrincipal(Boolean esPrincipal) {
        this.esPrincipal = (esPrincipal != null) ? esPrincipal : false;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
