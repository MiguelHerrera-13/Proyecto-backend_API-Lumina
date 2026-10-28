package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AuthResponseDTO;
import com.miguel.api_lumina.dto.ForgotPasswordRequestDTO;
import com.miguel.api_lumina.dto.ForgotPasswordResponseDTO;
import com.miguel.api_lumina.dto.LoginRequestDTO;
import com.miguel.api_lumina.dto.RefreshTokenRequestDTO;
import com.miguel.api_lumina.dto.ResetPasswordRequestDTO;
import com.miguel.api_lumina.dto.ResetPasswordResponseDTO;
import com.miguel.api_lumina.entity.PasswordResetTokenEntity;
import com.miguel.api_lumina.entity.RefreshTokenEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.PasswordResetTokenRepository;
import com.miguel.api_lumina.repository.UsuarioRepository;
import com.miguel.api_lumina.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${auth.reset-token.expiration-minutes:15}")
    private long tokenExpirationMinutes = 15;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this(usuarioRepository, passwordEncoder, jwtService, null, null);
    }

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this(usuarioRepository, passwordEncoder, jwtService, refreshTokenService, null);
    }

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       PasswordResetTokenRepository passwordResetTokenRepository) {
        this(usuarioRepository, passwordEncoder, jwtService, null, passwordResetTokenRepository);
    }

    @Autowired
    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       PasswordResetTokenRepository passwordResetTokenRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    public void setTokenExpirationMinutes(long tokenExpirationMinutes) {
        this.tokenExpirationMinutes = tokenExpirationMinutes;
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        UsuarioEntity usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String storedHash = usuario.getPasswordHash();
        boolean passwordMatches = false;

        // Comprobar si la contraseña almacenada ya tiene formato BCrypt estándar ($2a$, $2b$, $2y$)
        if (isBcryptHash(storedHash)) {
            passwordMatches = passwordEncoder.matches(request.getPassword(), storedHash);
        } else if (storedHash != null && storedHash.equals(request.getPassword())) {
            // Estrategia de transición segura (Lazy Migration):
            // Si la cuenta existía con contraseña previa y coincide exactamente,
            // se actualiza de forma transparente e inmediata a hash seguro BCrypt.
            passwordMatches = true;
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            usuarioRepository.save(usuario);
        }

        if (!passwordMatches) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getIdUsuario(), usuario.getRol());
        String refreshTokenStr = null;

        if (refreshTokenService != null) {
            RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(usuario);
            refreshTokenStr = refreshToken.getToken();
        }

        return new AuthResponseDTO(token, "Bearer", usuario.getEmail(), usuario.getRol(), refreshTokenStr);
    }

    @Transactional
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        if (refreshTokenService == null) {
            throw new IllegalStateException("RefreshTokenService no está configurado");
        }
        return refreshTokenService.refreshAccessToken(request);
    }

    @Transactional
    public ForgotPasswordResponseDTO forgotPassword(ForgotPasswordRequestDTO request) {
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByEmail(email);

        // Anti-enumeración:
        // Si el usuario existe y está activo, se genera el token y se almacena.
        // Si no existe o se encuentra inactivo, no se revela dicha información y se retorna la misma respuesta genérica.
        if (usuarioOpt.isPresent()) {
            UsuarioEntity usuario = usuarioOpt.get();
            if (Boolean.TRUE.equals(usuario.getActivo()) && passwordResetTokenRepository != null) {
                // Invalidar tokens previos sin usar para este usuario (política de seguridad de token único)
                List<PasswordResetTokenEntity> tokensPrevios = passwordResetTokenRepository.findByUsuarioAndUsadoFalse(usuario);
                for (PasswordResetTokenEntity previo : tokensPrevios) {
                    previo.setUsado(true);
                }
                passwordResetTokenRepository.saveAll(tokensPrevios);

                // Generar token único y seguro
                String token = UUID.randomUUID().toString();
                LocalDateTime fechaExpiracion = LocalDateTime.now().plusMinutes(tokenExpirationMinutes);

                PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(token, usuario, fechaExpiracion);
                passwordResetTokenRepository.save(resetToken);

                log.info("Token de recuperación de contraseña generado exitosamente para el usuario [{}]: {}", usuario.getEmail(), token);
            }
        }

        return new ForgotPasswordResponseDTO("Si el correo electrónico está registrado, recibirás un enlace o token para restablecer tu contraseña.");
    }

    @Transactional
    public ResetPasswordResponseDTO resetPassword(ResetPasswordRequestDTO request) {
        if (passwordResetTokenRepository == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Repositorio de tokens no configurado");
        }

        PasswordResetTokenEntity resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token de recuperación es inválido o no existe"));

        if (Boolean.TRUE.equals(resetToken.getUsado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token de recuperación ya ha sido utilizado");
        }

        if (resetToken.isExpirado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token de recuperación ha expirado");
        }

        UsuarioEntity usuario = resetToken.getUsuario();
        if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cuenta de usuario no está activa");
        }

        // Actualizar contraseña encriptada con BCrypt
        usuario.setPasswordHash(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);

        // Marcar token como utilizado (Token de un solo uso)
        resetToken.setUsado(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Contraseña actualizada exitosamente para el usuario [{}] mediante token de recuperación", usuario.getEmail());

        return new ResetPasswordResponseDTO("Contraseña restablecida exitosamente");
    }

    private boolean isBcryptHash(String hash) {
        if (hash == null || hash.length() != 60) {
            return false;
        }
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }
}
