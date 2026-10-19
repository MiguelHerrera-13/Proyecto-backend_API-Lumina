package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.AlertaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<AlertaEntity, Long> {

    List<AlertaEntity> findByMedico_IdUsuarioAndLeidaFalse(Long idMedico);

    List<AlertaEntity> findByMedico_IdUsuarioAndLeida(Long idMedico, Boolean leida);

}