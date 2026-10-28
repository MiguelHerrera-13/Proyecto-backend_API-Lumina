package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.SignosVitalesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignosVitalesRepository extends JpaRepository<SignosVitalesEntity, Long> {

    List<SignosVitalesEntity> findByPacienteIdPacienteOrderByFechaHoraDesc(Long idPaciente);
}
