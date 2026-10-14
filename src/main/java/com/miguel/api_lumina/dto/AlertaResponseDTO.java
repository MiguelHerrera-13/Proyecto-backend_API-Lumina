package com.miguel.api_lumina.dto;

import java.time.LocalDateTime;

public class AlertaResponseDTO {

    private Long id;
    private Long idReporte;
    private Long idMedico;
    private String nombrePaciente;
    private String mensajeAlerta;
    private Boolean urgente;
    private Boolean leida;
    private LocalDateTime fechaHora;

    public AlertaResponseDTO() {
    }

    public AlertaResponseDTO(Long id,
                             Long idReporte,
                             Long idMedico,
                             String nombrePaciente,
                             String mensajeAlerta,
                             Boolean urgente,
                             Boolean leida,
                             LocalDateTime fechaHora) {
        this.id = id;
        this.idReporte = idReporte;
        this.idMedico = idMedico;
        this.nombrePaciente = nombrePaciente;
        this.mensajeAlerta = mensajeAlerta;
        this.urgente = urgente;
        this.leida = leida;
        this.fechaHora = fechaHora;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Long idReporte) {
        this.idReporte = idReporte;
    }

    public Long getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(Long idMedico) {
        this.idMedico = idMedico;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public void setNombrePaciente(String nombrePaciente) {
        this.nombrePaciente = nombrePaciente;
    }

    public String getMensajeAlerta() {
        return mensajeAlerta;
    }

    public void setMensajeAlerta(String mensajeAlerta) {
        this.mensajeAlerta = mensajeAlerta;
    }

    public Boolean getUrgente() {
        return urgente;
    }

    public void setUrgente(Boolean urgente) {
        this.urgente = urgente;
    }

    public Boolean getLeida() {
        return leida;
    }

    public void setLeida(Boolean leida) {
        this.leida = leida;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}