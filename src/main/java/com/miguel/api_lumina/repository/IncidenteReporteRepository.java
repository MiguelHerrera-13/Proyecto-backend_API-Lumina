package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.IncidenteReporteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidenteReporteRepository extends JpaRepository<IncidenteReporteEntity, Long> {

    List<IncidenteReporteEntity> findByReporte_IdReporte(Long idReporte);
}

