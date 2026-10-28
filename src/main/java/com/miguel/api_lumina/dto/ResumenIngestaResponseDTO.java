package com.miguel.api_lumina.dto;

import java.util.List;

public class ResumenIngestaResponseDTO {

    private Long idReporte;
    private Integer totalAguaMl;
    private Integer cantidadIngestas;
    private Double porcentajeConsumoPromedio;
    private List<ControlIngestaResponseDTO> ingestas;

    public ResumenIngestaResponseDTO() {
    }

    public ResumenIngestaResponseDTO(Long idReporte, Integer totalAguaMl, Integer cantidadIngestas,
                                     Double porcentajeConsumoPromedio, List<ControlIngestaResponseDTO> ingestas) {
        this.idReporte = idReporte;
        this.totalAguaMl = totalAguaMl;
        this.cantidadIngestas = cantidadIngestas;
        this.porcentajeConsumoPromedio = porcentajeConsumoPromedio;
        this.ingestas = ingestas;
    }

    public Long getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Long idReporte) {
        this.idReporte = idReporte;
    }

    public Integer getTotalAguaMl() {
        return totalAguaMl;
    }

    public void setTotalAguaMl(Integer totalAguaMl) {
        this.totalAguaMl = totalAguaMl;
    }

    public Integer getCantidadIngestas() {
        return cantidadIngestas;
    }

    public void setCantidadIngestas(Integer cantidadIngestas) {
        this.cantidadIngestas = cantidadIngestas;
    }

    public Double getPorcentajeConsumoPromedio() {
        return porcentajeConsumoPromedio;
    }

    public void setPorcentajeConsumoPromedio(Double porcentajeConsumoPromedio) {
        this.porcentajeConsumoPromedio = porcentajeConsumoPromedio;
    }

    public List<ControlIngestaResponseDTO> getIngestas() {
        return ingestas;
    }

    public void setIngestas(List<ControlIngestaResponseDTO> ingestas) {
        this.ingestas = ingestas;
    }
}
