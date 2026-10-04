package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.ReporteDiarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteDiarioRepository extends JpaRepository<ReporteDiarioEntity, Long> {

    List<ReporteDiarioEntity> findByTurno_IdTurno(Long idTurno);

}
