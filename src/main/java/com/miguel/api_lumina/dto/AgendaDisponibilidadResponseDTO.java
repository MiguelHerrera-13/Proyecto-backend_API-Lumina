package com.miguel.api_lumina.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AgendaDisponibilidadResponseDTO {

    private Long profesionalId;
    private String nombreProfesional;
    private String rolProfesional;
    private LocalDateTime desde;
    private LocalDateTime hasta;
    private List<TurnoResponseDTO> turnosOcupados = new ArrayList<>();
    private List<IntervaloAgendaDTO> huecosLibres = new ArrayList<>();

    public AgendaDisponibilidadResponseDTO() {
    }

    public AgendaDisponibilidadResponseDTO(Long profesionalId, String nombreProfesional, String rolProfesional,
                                          LocalDateTime desde, LocalDateTime hasta,
                                          List<TurnoResponseDTO> turnosOcupados, List<IntervaloAgendaDTO> huecosLibres) {
        this.profesionalId = profesionalId;
        this.nombreProfesional = nombreProfesional;
        this.rolProfesional = rolProfesional;
        this.desde = desde;
        this.hasta = hasta;
        this.turnosOcupados = turnosOcupados != null ? turnosOcupados : new ArrayList<>();
        this.huecosLibres = huecosLibres != null ? huecosLibres : new ArrayList<>();
    }

    public Long getProfesionalId() {
        return profesionalId;
    }

    public void setProfesionalId(Long profesionalId) {
        this.profesionalId = profesionalId;
    }

    public String getNombreProfesional() {
        return nombreProfesional;
    }

    public void setNombreProfesional(String nombreProfesional) {
        this.nombreProfesional = nombreProfesional;
    }

    public String getRolProfesional() {
        return rolProfesional;
    }

    public void setRolProfesional(String rolProfesional) {
        this.rolProfesional = rolProfesional;
    }

    public LocalDateTime getDesde() {
        return desde;
    }

    public void setDesde(LocalDateTime desde) {
        this.desde = desde;
    }

    public LocalDateTime getHasta() {
        return hasta;
    }

    public void setHasta(LocalDateTime hasta) {
        this.hasta = hasta;
    }

    public List<TurnoResponseDTO> getTurnosOcupados() {
        return turnosOcupados;
    }

    public void setTurnosOcupados(List<TurnoResponseDTO> turnosOcupados) {
        this.turnosOcupados = turnosOcupados;
    }

    public List<IntervaloAgendaDTO> getHuecosLibres() {
        return huecosLibres;
    }

    public void setHuecosLibres(List<IntervaloAgendaDTO> huecosLibres) {
        this.huecosLibres = huecosLibres;
    }
}

