package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.PacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<PacienteEntity, Long> {

    @Query(value = "SELECT * FROM paciente WHERE activo = false ORDER BY fecha_baja DESC, id_paciente DESC", nativeQuery = true)
    List<PacienteEntity> findPacientesInactivos();

    @Query(value = "SELECT * FROM paciente WHERE id_paciente = :id", nativeQuery = true)
    Optional<PacienteEntity> findByIdAnyStatus(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE paciente SET activo = true, motivo_baja = NULL, fecha_baja = NULL, usuario_baja = NULL WHERE id_paciente = :id", nativeQuery = true)
    int reactivarPacienteById(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE paciente SET activo = false, motivo_baja = :motivo, fecha_baja = :fechaBaja, usuario_baja = :usuarioBaja WHERE id_paciente = :id", nativeQuery = true)
    int darDeBajaConAuditoria(
            @Param("id") Long id,
            @Param("motivo") String motivo,
            @Param("fechaBaja") LocalDateTime fechaBaja,
            @Param("usuarioBaja") String usuarioBaja);
}
