package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.TurnoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<TurnoEntity, Long> {
    List<TurnoEntity> findByCuidador_IdUsuario(Long idCuidador);
}
