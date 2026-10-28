package com.miguel.api_lumina.dto;

import java.util.List;

public class TurnoRecurrenteResponseDTO {

    private String idRecurrencia;
    private String patronRecurrencia;
    private int totalGenerados;
    private int totalOmitidosPorConflicto;
    private List<TurnoResponseDTO> turnosCreados;
    private List<TurnoConflictoDTO> turnosOmitidos;

    public TurnoRecurrenteResponseDTO() {
    }

    public TurnoRecurrenteResponseDTO(String idRecurrencia, String patronRecurrencia,
                                      int totalGenerados, int totalOmitidosPorConflicto,
                                      List<TurnoResponseDTO> turnosCreados,
                                      List<TurnoConflictoDTO> turnosOmitidos) {
        this.idRecurrencia = idRecurrencia;
        this.patronRecurrencia = patronRecurrencia;
        this.totalGenerados = totalGenerados;
        this.totalOmitidosPorConflicto = totalOmitidosPorConflicto;
        this.turnosCreados = turnosCreados;
        this.turnosOmitidos = turnosOmitidos;
    }

    public String getIdRecurrencia() {
        return idRecurrencia;
    }

    public void setIdRecurrencia(String idRecurrencia) {
        this.idRecurrencia = idRecurrencia;
    }

    public String getPatronRecurrencia() {
        return patronRecurrencia;
    }

    public void setPatronRecurrencia(String patronRecurrencia) {
        this.patronRecurrencia = patronRecurrencia;
    }

    public int getTotalGenerados() {
        return totalGenerados;
    }

    public void setTotalGenerados(int totalGenerados) {
        this.totalGenerados = totalGenerados;
    }

    public int getTotalOmitidosPorConflicto() {
        return totalOmitidosPorConflicto;
    }

    public void setTotalOmitidosPorConflicto(int totalOmitidosPorConflicto) {
        this.totalOmitidosPorConflicto = totalOmitidosPorConflicto;
    }

    public List<TurnoResponseDTO> getTurnosCreados() {
        return turnosCreados;
    }

    public void setTurnosCreados(List<TurnoResponseDTO> turnosCreados) {
        this.turnosCreados = turnosCreados;
    }

    public List<TurnoConflictoDTO> getTurnosOmitidos() {
        return turnosOmitidos;
    }

    public void setTurnosOmitidos(List<TurnoConflictoDTO> turnosOmitidos) {
        this.turnosOmitidos = turnosOmitidos;
    }
}
