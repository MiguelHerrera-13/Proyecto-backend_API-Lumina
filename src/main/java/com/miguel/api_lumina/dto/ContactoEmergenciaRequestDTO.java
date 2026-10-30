package com.miguel.api_lumina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ContactoEmergenciaRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "La relación o parentesco es obligatoria")
    @Size(max = 50, message = "La relación no puede superar los 50 caracteres")
    private String relacion;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{7,25}$", message = "El número telefónico no es válido")
    private String telefono;

    private Boolean esPrincipal = false;

    public ContactoEmergenciaRequestDTO() {
    }

    public ContactoEmergenciaRequestDTO(String nombre, String relacion, String telefono, Boolean esPrincipal) {
        this.nombre = nombre;
        this.relacion = relacion;
        this.telefono = telefono;
        this.esPrincipal = (esPrincipal != null) ? esPrincipal : false;
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
}
