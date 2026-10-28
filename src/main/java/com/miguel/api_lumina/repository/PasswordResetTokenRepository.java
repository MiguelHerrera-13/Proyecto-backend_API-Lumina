package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.PasswordResetTokenEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    Optional<PasswordResetTokenEntity> findByToken(String token);

    List<PasswordResetTokenEntity> findByUsuarioAndUsadoFalse(UsuarioEntity usuario);
}
