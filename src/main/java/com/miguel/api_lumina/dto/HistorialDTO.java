package com.miguel.api_lumina.dto;

import java.util.List;

public class HistorialDTO {

    private PacienteResponseDTO paciente;
    private List<UsuarioResponseDTO> usuariosRelacionados;
    private List<TurnoResponseDTO> turnos;
    private List<TratamientoResponseDTO> tratamientos;

    public HistorialDTO() {
    }

    public HistorialDTO(
            PacienteResponseDTO paciente,
            List<UsuarioResponseDTO> usuariosRelacionados,
            List<TurnoResponseDTO> turnos,
            List<TratamientoResponseDTO> tratamientos) {

        this.paciente = paciente;
        this.usuariosRelacionados = usuariosRelacionados;
        this.turnos = turnos;
        this.tratamientos = tratamientos;
    }

    public PacienteResponseDTO getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteResponseDTO paciente) {
        this.paciente = paciente;
    }

    public List<UsuarioResponseDTO> getUsuariosRelacionados() {
        return usuariosRelacionados;
    }

    public void setUsuariosRelacionados(List<UsuarioResponseDTO> usuariosRelacionados) {
        this.usuariosRelacionados = usuariosRelacionados;
    }

    public List<TurnoResponseDTO> getTurnos() {
        return turnos;
    }

    public void setTurnos(List<TurnoResponseDTO> turnos) {
        this.turnos = turnos;
    }

    public List<TratamientoResponseDTO> getTratamientos() {
        return tratamientos;
    }

    public void setTratamientos(List<TratamientoResponseDTO> tratamientos) {
        this.tratamientos = tratamientos;
    }
}