package com.miguel.api_lumina.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "alerta")
public class AlertaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long idAlerta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reporte", nullable = false)
    private ReporteDiarioEntity reporte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medico", nullable = false)
    private UsuarioEntity medico;

    @Column(name = "mensaje_alerta", nullable = false, columnDefinition = "TEXT")
    private String mensajeAlerta;

    @Column(name = "leida", nullable = false)
    private Boolean leida = false;

    public AlertaEntity() {
    }

    public Long getIdAlerta() {
        return idAlerta;
    }

    public void setIdAlerta(Long idAlerta) {
        this.idAlerta = idAlerta;
    }

    public ReporteDiarioEntity getReporte() {
        return reporte;
    }

    public void setReporte(ReporteDiarioEntity reporte) {
        this.reporte = reporte;
    }

    public UsuarioEntity getMedico() {
        return medico;
    }

    public void setMedico(UsuarioEntity medico) {
        this.medico = medico;
    }

    public String getMensajeAlerta() {
        return mensajeAlerta;
    }

    public void setMensajeAlerta(String mensajeAlerta) {
        this.mensajeAlerta = mensajeAlerta;
    }

    public Boolean getLeida() {
        return leida;
    }

    public void setLeida(Boolean leida) {
        this.leida = leida;
    }
}