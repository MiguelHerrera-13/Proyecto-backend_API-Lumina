package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.MedicamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicamentoRepository extends JpaRepository<MedicamentoEntity, Long> {

    List<MedicamentoEntity> findByTratamiento_IdTratamiento(Long idTratamiento);

}
