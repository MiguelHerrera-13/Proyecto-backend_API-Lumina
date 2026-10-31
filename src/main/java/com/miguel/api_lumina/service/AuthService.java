package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AuthResponseDTO;
import com.miguel.api_lumina.dto.ChangePasswordRequestDTO;
import com.miguel.api_lumina.dto.ChangePasswordResponseDTO;
import com.miguel.api_lumina.dto.ForgotPasswordRequestDTO;
import com.miguel.api_lumina.dto.ForgotPasswordResponseDTO;
import com.miguel.api_lumina.dto.LoginRequestDTO;
import com.miguel.api_lumina.dto.LogoutRequestDTO;
import com.miguel.api_lumina.dto.LogoutResponseDTO;
import com.miguel.api_lumina.dto.RefreshTokenRequestDTO;
import com.miguel.api_lumina.dto.ResetPasswordRequestDTO;
import com.miguel.api_lumina.dto.ResetPasswordResponseDTO;
import com.miguel.api_lumina.entity.PasswordResetTokenEntity;
import com.miguel.api_lumina.entity.RefreshTokenEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.PasswordResetTokenRepository;
import com.miguel.api_lumina.repository.UsuarioRepository;
import com.miguel.api_lumina.security.JwtService;
import com.miguel.api_lumina.security.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import com.miguel.api_lumina.dto.UsuarioResponseDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final Pattern PASSWORD_COMPLEXITY_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}$");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private LoginAttemptService loginAttemptService;

    @Value("${auth.reset-token.expiration-minutes:15}")
    private long tokenExpirationMinutes = 15;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this(usuarioRepository, passwordEncoder, jwtService, null, null, null);
    }

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this(usuarioRepository, passwordEncoder, jwtService, refreshTokenService, null, null);
    }

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       PasswordResetTokenRepository passwordResetTokenRepository) {
        this(usuarioRepository, passwordEncoder, jwtService, null, passwordResetTokenRepository, null);
    }

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       PasswordResetTokenRepository passwordResetTokenRepository) {
        this(usuarioRepository, passwordEncoder, jwtService, refreshTokenService, passwordResetTokenRepository, null);
    }

    @Autowired
    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       TokenBlacklistService tokenBlacklistService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Autowired(required = false)
    public void setLoginAttemptService(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    public void setTokenExpirationMinutes(long tokenExpirationMinutes) {
        this.tokenExpirationMinutes = tokenExpirationMinutes;
    }

    private static final int MAX_INTENTOS = 5;

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        UsuarioEntity usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        // B-18: Verificar si la cuenta está bloqueada temporalmente
        if (usuario.getBloqueadoHasta() != null) {
            if (usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Cuenta bloqueada temporalmente. Intente nuevamente más tarde.");
            } else {
                // Desbloqueo automático una vez transcurridos los 15 minutos
                if (loginAttemptService != null) {
                    loginAttemptService.resetearIntentos(usuario);
                }
            }
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
            // B-18: Guardar intento fallido en transacción independiente (REQUIRES_NEW)
            if (loginAttemptService != null) {
                loginAttemptService.registrarIntentoFallido(usuario);
            }
            if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Cuenta bloqueada temporalmente. Intente nuevamente más tarde.");
            }
            throw new BadCredentialsException("Credenciales inválidas");
        }

        // B-18: Login exitoso — resetear contador en transacción independiente
        if (loginAttemptService != null) {
            loginAttemptService.resetearIntentos(usuario);
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
    public LogoutResponseDTO logout(String authHeader, LogoutRequestDTO request) {
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7).trim();
        }

        // 1. Invalidar access token en la lista negra en memoria
        if (accessToken != null && !accessToken.isBlank() && tokenBlacklistService != null) {
            tokenBlacklistService.blacklistToken(accessToken);
        }

        // 2. Revocar refresh token explícito si se proporcionó en el cuerpo
        String refreshToken = request != null ? request.getRefreshToken() : null;
        if (refreshToken != null && !refreshToken.isBlank() && refreshTokenService != null) {
            refreshTokenService.revokeRefreshToken(refreshToken.trim());
        }

        // 3. Si se dispone del access token, también revocar todos los refresh tokens activos del usuario en BD
        if (accessToken != null && !accessToken.isBlank() && refreshTokenService != null) {
            try {
                String email = jwtService.extractEmail(accessToken);
                if (email != null && !email.isBlank()) {
                    usuarioRepository.findByEmail(email).ifPresent(refreshTokenService::revokeAllUserTokens);
                }
            } catch (Exception ignored) {
            }
        }

        log.info("Cierre de sesión (logout) ejecutado exitosamente");
        return new LogoutResponseDTO("Sesión cerrada exitosamente");
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

    @Transactional
    public ChangePasswordResponseDTO changePassword(Authentication authentication, String authHeader, ChangePasswordRequestDTO request) {
        String email = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            email = authentication.getName();
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (tokenBlacklistService == null || !tokenBlacklistService.isBlacklisted(token)) {
                try {
                    email = jwtService.extractEmail(token);
                } catch (Exception ignored) {
                }
            }
        }

        return changePassword(email, request);
    }

    @Transactional
    public ChangePasswordResponseDTO changePassword(String email, ChangePasswordRequestDTO request) {
        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acceso no autorizado: se requiere autenticación válida");
        }

        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La cuenta de usuario no está activa");
        }

        // 1. Validar que la contraseña actual coincida (Criterio de Aceptación 1)
        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual no coincide");
        }

        // 2. Validar que la nueva contraseña y la confirmación coincidan
        if (!request.getNuevaPassword().equals(request.getConfirmarPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La confirmación de la contraseña no coincide");
        }

        // 3. Validar políticas de complejidad (mínimo 8 caracteres, números, mayúsculas)
        if (!PASSWORD_COMPLEXITY_PATTERN.matcher(request.getNuevaPassword()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe tener al menos 8 caracteres, incluir al menos una letra mayúscula y un número");
        }

        // 4. Actualizar contraseña hasheada con BCrypt (Criterio de Aceptación 2)
        usuario.setPasswordHash(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);

        // 5. Revocar refresh tokens activos previos por seguridad
        if (refreshTokenService != null) {
            refreshTokenService.revokeAllUserTokens(usuario);
        }

        log.info("Contraseña actualizada exitosamente para el usuario [{}] mediante cambio de contraseña", email);
        return new ChangePasswordResponseDTO("Contraseña actualizada exitosamente");
    }

    private boolean isBcryptHash(String hash) {
        if (hash == null || hash.length() != 60) {
            return false;
        }
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerUsuarioActual() {
        return obtenerUsuarioActual(SecurityContextHolder.getContext().getAuthentication());
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerUsuarioActual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acceso no autorizado: se requiere autenticación válida");
        }

        String email = authentication.getName();
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La cuenta de usuario no está activa");
        }

        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol());
        dto.setActivo(usuario.getActivo());
        return dto;
    }
}
