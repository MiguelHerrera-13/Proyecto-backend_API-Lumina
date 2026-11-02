package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.TokenVerificacionEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenVerificacionRepository extends JpaRepository<TokenVerificacionEntity, Long> {
    Optional<TokenVerificacionEntity> findByToken(String token);
    Optional<TokenVerificacionEntity> findTopByUsuarioOrderByFechaCreacionDesc(UsuarioEntity usuario);
}
