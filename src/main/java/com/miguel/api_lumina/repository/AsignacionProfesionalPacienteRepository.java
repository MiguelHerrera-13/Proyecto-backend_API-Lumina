package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.AsignacionProfesionalPacienteEntity;
import com.miguel.api_lumina.entity.TipoAsignacionProfesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionProfesionalPacienteRepository
        extends JpaRepository<AsignacionProfesionalPacienteEntity, Long> {

    List<AsignacionProfesionalPacienteEntity>
    findByPaciente_IdPacienteAndActivaTrue(Long idPaciente);

    Optional<AsignacionProfesionalPacienteEntity>
    findFirstByPaciente_IdPacienteAndTipoAsignacionAndActivaTrue(
            Long idPaciente,
            TipoAsignacionProfesional tipoAsignacion
    );

    boolean existsByPaciente_IdPacienteAndUsuario_IdUsuarioAndTipoAsignacionAndActivaTrue(
            Long idPaciente,
            Long idUsuario,
            TipoAsignacionProfesional tipoAsignacion
    );

    List<AsignacionProfesionalPacienteEntity>
    findByPaciente_IdPacienteOrderByFechaDesdeDesc(Long idPaciente);
}