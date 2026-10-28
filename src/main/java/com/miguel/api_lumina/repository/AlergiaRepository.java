package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.AlergiaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlergiaRepository extends JpaRepository<AlergiaEntity, Long> {

    List<AlergiaEntity> findByPacienteIdPaciente(Long idPaciente);

    Optional<AlergiaEntity> findByIdAlergiaAndPacienteIdPaciente(Long idAlergia, Long idPaciente);

    boolean existsByPacienteIdPacienteAndTipoIgnoreCase(Long idPaciente, String tipo);

    boolean existsByPacienteIdPacienteAndTipoIgnoreCaseAndIdAlergiaNot(Long idPaciente, String tipo, Long idAlergia);
}
