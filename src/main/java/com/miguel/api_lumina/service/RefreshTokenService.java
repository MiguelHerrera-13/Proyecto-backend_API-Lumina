package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AuthResponseDTO;
import com.miguel.api_lumina.dto.RefreshTokenRequestDTO;
import com.miguel.api_lumina.entity.RefreshTokenEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.RefreshTokenRepository;
import com.miguel.api_lumina.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${jwt.refresh-expiration-days:7}")
    private long refreshExpirationDays = 7;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    public void setRefreshExpirationDays(long refreshExpirationDays) {
        this.refreshExpirationDays = refreshExpirationDays;
    }

    @Transactional
    public RefreshTokenEntity createRefreshToken(UsuarioEntity usuario) {
        String token = UUID.randomUUID().toString();
        LocalDateTime fechaExpiracion = LocalDateTime.now().plusDays(refreshExpirationDays);

        RefreshTokenEntity refreshToken = new RefreshTokenEntity(token, usuario, fechaExpiracion);
        RefreshTokenEntity saved = refreshTokenRepository.save(refreshToken);

        log.info("Refresh token generado exitosamente para el usuario [{}]: {}", usuario.getEmail(), token);
        return saved;
    }

    @Transactional
    public AuthResponseDTO refreshAccessToken(RefreshTokenRequestDTO request) {
        String tokenStr = request.getRefreshToken();
        if (tokenStr == null || tokenStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inexistente o inválido");
        }

        RefreshTokenEntity currentToken = refreshTokenRepository.findByToken(tokenStr.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inexistente o inválido"));

        if (currentToken.isRevocado()) {
            log.warn("Intento de reutilización de refresh token revocado para el usuario [{}]",
                    currentToken.getUsuario() != null ? currentToken.getUsuario().getEmail() : "desconocido");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El refresh token ya ha sido utilizado o revocado");
        }

        if (currentToken.isExpirado()) {
            currentToken.setRevocado(true);
            refreshTokenRepository.save(currentToken);
            log.info("Refresh token expirado para el usuario [{}]", currentToken.getUsuario().getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El refresh token ha expirado");
        }

        UsuarioEntity usuario = currentToken.getUsuario();
        if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La cuenta de usuario asociada no se encuentra activa");
        }

        // 1. Rotación del Refresh Token: Marcar el token anterior como revocado para mitigar ataques de repetición
        currentToken.setRevocado(true);
        refreshTokenRepository.save(currentToken);

        // 2. Generar y persistir un nuevo refresh token
        String newUUID = UUID.randomUUID().toString();
        LocalDateTime newExpiration = LocalDateTime.now().plusDays(refreshExpirationDays);
        RefreshTokenEntity newRefreshToken = new RefreshTokenEntity(newUUID, usuario, newExpiration);
        RefreshTokenEntity savedNewRefreshToken = refreshTokenRepository.save(newRefreshToken);

        // 3. Emitir nuevo access token JWT
        String newAccessToken = jwtService.generateToken(usuario.getEmail(), usuario.getIdUsuario(), usuario.getRol());

        log.info("Refresh token rotado exitosamente para el usuario [{}]. Nuevo refresh token: {}", usuario.getEmail(), newUUID);

        return new AuthResponseDTO(newAccessToken, "Bearer", usuario.getEmail(), usuario.getRol(), savedNewRefreshToken.getToken());
    }

    @Transactional(readOnly = true)
    public Optional<RefreshTokenEntity> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
}
