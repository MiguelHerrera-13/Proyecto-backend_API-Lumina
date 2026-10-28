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

    List<TurnoEntity> findByIdRecurrencia(String idRecurrencia);

    @Query("SELECT COUNT(t) > 0 FROM TurnoEntity t WHERE t.cuidador.idUsuario = :idCuidador AND t.fechaHoraInicio < :fin AND t.fechaHoraFin > :inicio")
    boolean existeSolapamientoCuidador(@Param("idCuidador") Long idCuidador,
                                      @Param("inicio") LocalDateTime inicio,
                                      @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(t) > 0 FROM TurnoEntity t WHERE t.paciente.idPaciente = :idPaciente AND t.fechaHoraInicio < :fin AND t.fechaHoraFin > :inicio")
    boolean existeSolapamientoPaciente(@Param("idPaciente") Long idPaciente,
                                      @Param("inicio") LocalDateTime inicio,
                                      @Param("fin") LocalDateTime fin);
}
