package com.miguel.api_lumina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "control_ingesta")
@SQLDelete(sql = "UPDATE control_ingesta SET activo = false WHERE id_control_ingesta=?")
@SQLRestriction("activo=true")
public class ControlIngestaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_control_ingesta")
    private Long idControlIngesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reporte", nullable = false)
    private ReporteDiarioEntity reporte;

    @Column(name = "tipo_comida", nullable = false, length = 50)
    private String tipoComida;

    @Column(name = "descripcion_comida", columnDefinition = "TEXT")
    private String descripcionComida;

    @Column(name = "porcentaje_consumido", nullable = false)
    private Integer porcentajeConsumido;

    @Column(name = "agua_ml", nullable = false)
    private Integer aguaMl;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "activo")
    private Boolean activo = true;

    public ControlIngestaEntity() {
    }

    public ControlIngestaEntity(Long idControlIngesta, ReporteDiarioEntity reporte, String tipoComida,
                                String descripcionComida, Integer porcentajeConsumido, Integer aguaMl,
                                LocalDateTime fechaHora, String observaciones, Boolean activo) {
        this.idControlIngesta = idControlIngesta;
        this.reporte = reporte;
        this.tipoComida = tipoComida;
        this.descripcionComida = descripcionComida;
        this.porcentajeConsumido = porcentajeConsumido;
        this.aguaMl = aguaMl;
        this.fechaHora = fechaHora;
        this.observaciones = observaciones;
        this.activo = activo;
    }

    public Long getIdControlIngesta() {
        return idControlIngesta;
    }

    public void setIdControlIngesta(Long idControlIngesta) {
        this.idControlIngesta = idControlIngesta;
    }

    public ReporteDiarioEntity getReporte() {
        return reporte;
    }

    public void setReporte(ReporteDiarioEntity reporte) {
        this.reporte = reporte;
    }

    public String getTipoComida() {
        return tipoComida;
    }

    public void setTipoComida(String tipoComida) {
        this.tipoComida = tipoComida;
    }

    public String getDescripcionComida() {
        return descripcionComida;
    }

    public void setDescripcionComida(String descripcionComida) {
        this.descripcionComida = descripcionComida;
    }

    public Integer getPorcentajeConsumido() {
        return porcentajeConsumido;
    }

    public void setPorcentajeConsumido(Integer porcentajeConsumido) {
        this.porcentajeConsumido = porcentajeConsumido;
    }

    public Integer getAguaMl() {
        return aguaMl;
    }

    public void setAguaMl(Integer aguaMl) {
        this.aguaMl = aguaMl;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}

