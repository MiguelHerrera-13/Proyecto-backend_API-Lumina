package com.miguel.api_lumina.dto;

public class ContactoEmergenciaResponseDTO {

    private Long idContacto;
    private Long idPaciente;
    private String nombre;
    private String relacion;
    private String telefono;
    private Boolean esPrincipal;
    private Boolean activo;

    public ContactoEmergenciaResponseDTO() {
    }

    public ContactoEmergenciaResponseDTO(Long idContacto, Long idPaciente, String nombre, String relacion, String telefono, Boolean esPrincipal, Boolean activo) {
        this.idContacto = idContacto;
        this.idPaciente = idPaciente;
        this.nombre = nombre;
        this.relacion = relacion;
        this.telefono = telefono;
        this.esPrincipal = esPrincipal;
        this.activo = activo;
    }

    public Long getIdContacto() {
        return idContacto;
    }

    public void setIdContacto(Long idContacto) {
        this.idContacto = idContacto;
    }

    public Long getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Long idPaciente) {
        this.idPaciente = idPaciente;
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
        this.esPrincipal = esPrincipal;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
