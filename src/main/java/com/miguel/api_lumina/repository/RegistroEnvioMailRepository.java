package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.RegistroEnvioMailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroEnvioMailRepository extends JpaRepository<RegistroEnvioMailEntity, Long> {
    List<RegistroEnvioMailEntity> findByDestinatarioOrderByFechaEnvioDesc(String destinatario);
}

