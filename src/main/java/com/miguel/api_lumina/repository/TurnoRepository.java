package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.TurnoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<TurnoEntity, Long> {
    List<TurnoEntity> findByCuidador_IdUsuario(Long idCuidador);
    List<TurnoEntity> findByPaciente_IdPaciente(Long idPaciente);

    @Query("SELECT t FROM TurnoEntity t " +
           "WHERE (:profesionalId IS NULL OR t.medico.idUsuario = :profesionalId OR t.cuidador.idUsuario = :profesionalId) " +
           "AND t.estado != 'CANCELADO' " +
           "AND t.fechaHoraInicio < :hasta AND t.fechaHoraFin > :desde " +
           "ORDER BY t.fechaHoraInicio ASC")
    List<TurnoEntity> findAgendaProfesional(
            @Param("profesionalId") Long profesionalId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );
}
