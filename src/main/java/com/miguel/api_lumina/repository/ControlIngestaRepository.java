package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.ControlIngestaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ControlIngestaRepository extends JpaRepository<ControlIngestaEntity, Long> {

    List<ControlIngestaEntity> findByReporte_IdReporte(Long idReporte);

    @Query("SELECT COALESCE(SUM(c.aguaMl), 0) FROM ControlIngestaEntity c WHERE c.reporte.idReporte = :idReporte")
    Integer sumarAguaMlPorReporte(@Param("idReporte") Long idReporte);
}
