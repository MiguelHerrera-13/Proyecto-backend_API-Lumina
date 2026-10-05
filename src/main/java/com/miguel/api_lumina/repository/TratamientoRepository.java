package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.TratamientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TratamientoRepository extends JpaRepository<TratamientoEntity, Long> {

    List<TratamientoEntity> findByPaciente_IdPaciente(Long idPaciente);

    List<TratamientoEntity> findByMedico_IdUsuario(Long idMedico);

}
