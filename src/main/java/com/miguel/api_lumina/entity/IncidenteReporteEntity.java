package com.miguel.api_lumina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidente_reporte")
@SQLDelete(sql = "UPDATE incidente_reporte SET activo = false WHERE id_incidente=?")
@SQLRestriction("activo=true")
public class IncidenteReporteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incidente")
    private Long idIncidente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reporte", nullable = false)
    private ReporteDiarioEntity reporte;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "tipo_incidente", nullable = false, length = 50)
    private String tipoIncidente;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "severidad", nullable = false, length = 20)
    private String severidad;

    @Column(name = "activo")
    private Boolean activo = true;

    public IncidenteReporteEntity() {
    }

    public IncidenteReporteEntity(Long idIncidente, ReporteDiarioEntity reporte, LocalDateTime fechaHora,
                                  String tipoIncidente, String descripcion, String severidad, Boolean activo) {
        this.idIncidente = idIncidente;
        this.reporte = reporte;
        this.fechaHora = fechaHora;
        this.tipoIncidente = tipoIncidente;
        this.descripcion = descripcion;
        this.severidad = severidad;
        this.activo = activo;
    }

    public Long getIdIncidente() {
        return idIncidente;
    }

    public void setIdIncidente(Long idIncidente) {
        this.idIncidente = idIncidente;
    }

    public ReporteDiarioEntity getReporte() {
        return reporte;
    }

    public void setReporte(ReporteDiarioEntity reporte) {
        this.reporte = reporte;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getTipoIncidente() {
        return tipoIncidente;
    }

    public void setTipoIncidente(String tipoIncidente) {
        this.tipoIncidente = tipoIncidente;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}

