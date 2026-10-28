package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.TurnoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<TurnoEntity, Long>, JpaSpecificationExecutor<TurnoEntity> {

    List<TurnoEntity> findByCuidador_IdUsuario(Long idCuidador);

    List<TurnoEntity> findByPaciente_IdPaciente(Long idPaciente);

    List<TurnoEntity> findByIdRecurrencia(String idRecurrencia);

    @Query("SELECT COUNT(t) > 0 FROM TurnoEntity t WHERE t.cuidador.idUsuario = :idCuidador AND t.estado != 'CANCELADO' AND t.fechaHoraInicio < :fin AND t.fechaHoraFin > :inicio")
    boolean existeSolapamientoCuidador(@Param("idCuidador") Long idCuidador,
                                      @Param("inicio") LocalDateTime inicio,
                                      @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(t) > 0 FROM TurnoEntity t WHERE t.paciente.idPaciente = :idPaciente AND t.estado != 'CANCELADO' AND t.fechaHoraInicio < :fin AND t.fechaHoraFin > :inicio")
    boolean existeSolapamientoPaciente(@Param("idPaciente") Long idPaciente,
                                      @Param("inicio") LocalDateTime inicio,
                                      @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(t) > 0 FROM TurnoEntity t " +
           "WHERE t.idTurno != :idTurno " +
           "AND t.medico.idUsuario = :idMedico " +
           "AND t.estado != 'CANCELADO' " +
           "AND t.fechaHoraInicio < :fechaFin " +
           "AND t.fechaHoraFin > :fechaInicio")
    boolean existsSuperposicionMedico(
            @Param("idTurno") Long idTurno,
            @Param("idMedico") Long idMedico,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("SELECT COUNT(t) > 0 FROM TurnoEntity t " +
           "WHERE t.idTurno != :idTurno " +
           "AND t.cuidador.idUsuario = :idCuidador " +
           "AND t.estado != 'CANCELADO' " +
           "AND t.fechaHoraInicio < :fechaFin " +
           "AND t.fechaHoraFin > :fechaInicio")
    boolean existsSuperposicionCuidador(
            @Param("idTurno") Long idTurno,
            @Param("idCuidador") Long idCuidador,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

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
