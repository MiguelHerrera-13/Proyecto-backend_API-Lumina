package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.RefreshTokenEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByToken(String token);

    List<RefreshTokenEntity> findByUsuario(UsuarioEntity usuario);

    List<RefreshTokenEntity> findByUsuarioAndRevocadoFalse(UsuarioEntity usuario);

    void deleteByUsuario(UsuarioEntity usuario);
}
