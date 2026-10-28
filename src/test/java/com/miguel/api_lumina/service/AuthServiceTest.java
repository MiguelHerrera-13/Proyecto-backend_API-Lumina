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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(usuarioRepository, passwordEncoder, jwtService, refreshTokenService, passwordResetTokenRepository, tokenBlacklistService);
    }

    @Test
    @DisplayName("Login exitoso con contraseña BCrypt retorna token JWT y refresh token")
    public void testLogin_Success_BcryptPassword() {
        String rawPassword = "passwordSegura123";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);
        usuario.setEmail("doctor@test.com");
        usuario.setPasswordHash(hashedPassword);
        usuario.setRol("MEDICO");
        usuario.setActivo(true);

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity("refresh-token-uuid-1", usuario, LocalDateTime.now().plusDays(7));

        when(usuarioRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken("doctor@test.com", 1L, "MEDICO")).thenReturn("fake.jwt.token");
        when(refreshTokenService.createRefreshToken(usuario)).thenReturn(refreshTokenEntity);

        LoginRequestDTO request = new LoginRequestDTO("doctor@test.com", rawPassword);
        AuthResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTipo());
        assertEquals("doctor@test.com", response.getEmail());
        assertEquals("MEDICO", response.getRol());
        assertEquals("refresh-token-uuid-1", response.getRefreshToken());

        verify(usuarioRepository, never()).save(any(UsuarioEntity.class));
        verify(refreshTokenService).createRefreshToken(usuario);
    }

    @Test
    @DisplayName("Login exitoso con cuenta preexistente en texto plano: migra transparentemente a BCrypt")
    public void testLogin_Success_LegacyPlaintext_MigratesToBcrypt() {
        String legacyPassword = "claveAntigua123";

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(2L);
        usuario.setEmail("legacy@test.com");
        usuario.setPasswordHash(legacyPassword); // Texto plano previo
        usuario.setRol("CUIDADOR");
        usuario.setActivo(true);

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity("refresh-token-uuid-2", usuario, LocalDateTime.now().plusDays(7));

        when(usuarioRepository.findByEmail("legacy@test.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken("legacy@test.com", 2L, "CUIDADOR")).thenReturn("fake.jwt.migrated");
        when(refreshTokenService.createRefreshToken(usuario)).thenReturn(refreshTokenEntity);

        LoginRequestDTO request = new LoginRequestDTO("legacy@test.com", legacyPassword);
        AuthResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake.jwt.migrated", response.getToken());
        assertEquals("refresh-token-uuid-2", response.getRefreshToken());

        // Comprobar que se guardó la entidad con la contraseña migrada a BCrypt
        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());
        UsuarioEntity updatedUser = captor.getValue();

        assertNotEquals(legacyPassword, updatedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches(legacyPassword, updatedUser.getPasswordHash()));
        assertTrue(updatedUser.getPasswordHash().startsWith("$2a$"));
    }

    @Test
    @DisplayName("Login fallido con contraseña incorrecta")
    public void testLogin_Failure_WrongPassword() {
        String hashedPassword = passwordEncoder.encode("correcta123");

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(3L);
        usuario.setEmail("usuario@test.com");
        usuario.setPasswordHash(hashedPassword);
        usuario.setRol("ADMIN");
        usuario.setActivo(true);

        when(usuarioRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(usuario));

        LoginRequestDTO request = new LoginRequestDTO("usuario@test.com", "incorrecta456");

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any(), any(), any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    @Test
    @DisplayName("Login fallido con email inexistente")
    public void testLogin_Failure_UserNotFound() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        LoginRequestDTO request = new LoginRequestDTO("noexiste@test.com", "algunaClave");

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any(), any(), any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    @Test
    @DisplayName("Login fallido con usuario inactivo")
    public void testLogin_Failure_InactiveUser() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(4L);
        usuario.setEmail("inactivo@test.com");
        usuario.setPasswordHash(passwordEncoder.encode("clave123"));
        usuario.setRol("MEDICO");
        usuario.setActivo(false); // Inactivo

        when(usuarioRepository.findByEmail("inactivo@test.com")).thenReturn(Optional.of(usuario));

        LoginRequestDTO request = new LoginRequestDTO("inactivo@test.com", "clave123");

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any(), any(), any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    @Test
    @DisplayName("refreshToken delega correctamente a RefreshTokenService")
    public void testRefreshToken_DelegatesToService() {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("token-123");
        AuthResponseDTO expectedResponse = new AuthResponseDTO("jwt-nuevo", "Bearer", "test@test.com", "ADMIN", "refresh-nuevo");

        when(refreshTokenService.refreshAccessToken(request)).thenReturn(expectedResponse);

        AuthResponseDTO actualResponse = authService.refreshToken(request);

        assertNotNull(actualResponse);
        assertEquals("jwt-nuevo", actualResponse.getToken());
        assertEquals("refresh-nuevo", actualResponse.getRefreshToken());
        verify(refreshTokenService).refreshAccessToken(request);
    }

    // --- Pruebas de B-15: Logout y Revocación de Tokens ---

    @Test
    @DisplayName("logout agrega access token a lista negra y revoca refresh tokens")
    public void testLogout_Success_WithTokenAndRefresh() {
        String authHeader = "Bearer mi.jwt.activo";
        LogoutRequestDTO request = new LogoutRequestDTO("refresh-token-123");

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail("medico@lumina.com");

        when(jwtService.extractEmail("mi.jwt.activo")).thenReturn("medico@lumina.com");
        when(usuarioRepository.findByEmail("medico@lumina.com")).thenReturn(Optional.of(usuario));

        LogoutResponseDTO response = authService.logout(authHeader, request);

        assertNotNull(response);
        assertEquals("Sesión cerrada exitosamente", response.getMensaje());

        // Comprobar que el access token se agregó a la lista negra
        verify(tokenBlacklistService).blacklistToken("mi.jwt.activo");

        // Comprobar que se revocó el refresh token específico
        verify(refreshTokenService).revokeRefreshToken("refresh-token-123");

        // Comprobar que se revocaron todos los refresh tokens activos del usuario
        verify(refreshTokenService).revokeAllUserTokens(usuario);
    }

    @Test
    @DisplayName("logout funciona correctamente cuando solo se envía el refresh token en el cuerpo")
    public void testLogout_OnlyRefreshToken() {
        LogoutRequestDTO request = new LogoutRequestDTO("solo-refresh-456");

        LogoutResponseDTO response = authService.logout(null, request);

        assertNotNull(response);
        assertEquals("Sesión cerrada exitosamente", response.getMensaje());
        verify(tokenBlacklistService, never()).blacklistToken(anyString());
        verify(refreshTokenService).revokeRefreshToken("solo-refresh-456");
    }

    // --- Pruebas de B-16: Recuperación y reseteo de contraseña por token ---

    @Test
    @DisplayName("forgotPassword genera token temporal (15 min) e invalida tokens previos si el usuario existe y está activo")
    public void testForgotPassword_Success_GeneratesToken() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(10L);
        usuario.setEmail("activo@lumina.com");
        usuario.setActivo(true);

        PasswordResetTokenEntity tokenPrevio = new PasswordResetTokenEntity("token-viejo", usuario, LocalDateTime.now().plusMinutes(5));
        tokenPrevio.setUsado(false);

        when(usuarioRepository.findByEmail("activo@lumina.com")).thenReturn(Optional.of(usuario));
        when(passwordResetTokenRepository.findByUsuarioAndUsadoFalse(usuario)).thenReturn(List.of(tokenPrevio));

        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO("activo@lumina.com");
        ForgotPasswordResponseDTO response = authService.forgotPassword(request);

        assertNotNull(response);
        assertEquals("Si el correo electrónico está registrado, recibirás un enlace o token para restablecer tu contraseña.", response.getMensaje());

        // Verificar que el token previo se invalidó
        assertTrue(tokenPrevio.getUsado());
        verify(passwordResetTokenRepository).saveAll(List.of(tokenPrevio));

        // Verificar que se guardó un nuevo token con expiración de 15 minutos
        ArgumentCaptor<PasswordResetTokenEntity> tokenCaptor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        PasswordResetTokenEntity nuevoToken = tokenCaptor.getValue();

        assertNotNull(nuevoToken.getToken());
        assertEquals(usuario, nuevoToken.getUsuario());
        assertFalse(nuevoToken.getUsado());
        assertTrue(nuevoToken.getFechaExpiracion().isAfter(LocalDateTime.now().plusMinutes(14)));
        assertTrue(nuevoToken.getFechaExpiracion().isBefore(LocalDateTime.now().plusMinutes(16)));
    }

    @Test
    @DisplayName("forgotPassword con anti-enumeración: si el email no existe retorna 200 sin revelar existencia ni crear token")
    public void testForgotPassword_AntiEnumeration_NonExistentEmail() {
        when(usuarioRepository.findByEmail("noexiste@lumina.com")).thenReturn(Optional.empty());

        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO("noexiste@lumina.com");
        ForgotPasswordResponseDTO response = authService.forgotPassword(request);

        assertNotNull(response);
        assertEquals("Si el correo electrónico está registrado, recibirás un enlace o token para restablecer tu contraseña.", response.getMensaje());

        // No debe guardar ningún token
        verify(passwordResetTokenRepository, never()).save(any(PasswordResetTokenEntity.class));
    }

    @Test
    @DisplayName("forgotPassword con anti-enumeración: si el usuario está inactivo retorna 200 sin generar token")
    public void testForgotPassword_AntiEnumeration_InactiveUser() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(11L);
        usuario.setEmail("inactivo@lumina.com");
        usuario.setActivo(false);

        when(usuarioRepository.findByEmail("inactivo@lumina.com")).thenReturn(Optional.of(usuario));

        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO("inactivo@lumina.com");
        ForgotPasswordResponseDTO response = authService.forgotPassword(request);

        assertNotNull(response);
        assertEquals("Si el correo electrónico está registrado, recibirás un enlace o token para restablecer tu contraseña.", response.getMensaje());

        // No debe generar token
        verify(passwordResetTokenRepository, never()).save(any(PasswordResetTokenEntity.class));
    }

    @Test
    @DisplayName("resetPassword actualiza la contraseña con BCrypt y marca el token como usado (un solo uso)")
    public void testResetPassword_Success() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(12L);
        usuario.setEmail("recuperar@lumina.com");
        usuario.setPasswordHash("$2a$10$antiguaHash...");
        usuario.setActivo(true);

        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(
                "token-valido-123",
                usuario,
                LocalDateTime.now().plusMinutes(15)
        );
        resetToken.setUsado(false);

        when(passwordResetTokenRepository.findByToken("token-valido-123")).thenReturn(Optional.of(resetToken));

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("token-valido-123", "nuevaPasswordSegura456");
        ResetPasswordResponseDTO response = authService.resetPassword(request);

        assertNotNull(response);
        assertEquals("Contraseña restablecida exitosamente", response.getMensaje());

        // Validar que el token quedó marcado como usado
        assertTrue(resetToken.getUsado());
        verify(passwordResetTokenRepository).save(resetToken);

        // Validar que la nueva contraseña se encriptó con BCrypt
        ArgumentCaptor<UsuarioEntity> userCaptor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(userCaptor.capture());
        UsuarioEntity updatedUser = userCaptor.getValue();

        assertTrue(passwordEncoder.matches("nuevaPasswordSegura456", updatedUser.getPasswordHash()));
        assertTrue(updatedUser.getPasswordHash().startsWith("$2a$"));
    }

    @Test
    @DisplayName("resetPassword falla si el token ya fue utilizado (token de un solo uso)")
    public void testResetPassword_Failure_AlreadyUsed() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(13L);
        usuario.setActivo(true);

        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(
                "token-ya-usado",
                usuario,
                LocalDateTime.now().plusMinutes(10)
        );
        resetToken.setUsado(true); // Ya fue usado previamente

        when(passwordResetTokenRepository.findByToken("token-ya-usado")).thenReturn(Optional.of(resetToken));

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("token-ya-usado", "otraPassword");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.resetPassword(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("El token de recuperación ya ha sido utilizado", ex.getReason());

        verify(usuarioRepository, never()).save(any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("resetPassword falla si el token está expirado (vencimiento estricto)")
    public void testResetPassword_Failure_ExpiredToken() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(14L);
        usuario.setActivo(true);

        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(
                "token-expirado",
                usuario,
                LocalDateTime.now().minusMinutes(1) // Expirado hace un minuto
        );
        resetToken.setUsado(false);

        when(passwordResetTokenRepository.findByToken("token-expirado")).thenReturn(Optional.of(resetToken));

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("token-expirado", "otraPassword");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.resetPassword(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("El token de recuperación ha expirado", ex.getReason());

        verify(usuarioRepository, never()).save(any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("resetPassword falla si el token no existe en el repositorio")
    public void testResetPassword_Failure_TokenNotFound() {
        when(passwordResetTokenRepository.findByToken("token-inexistente")).thenReturn(Optional.empty());

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("token-inexistente", "otraPassword");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.resetPassword(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("El token de recuperación es inválido o no existe", ex.getReason());

        verify(usuarioRepository, never()).save(any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("resetPassword falla si el usuario asociado al token está inactivo")
    public void testResetPassword_Failure_InactiveUser() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(15L);
        usuario.setActivo(false); // Inactivo

        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(
                "token-usuario-inactivo",
                usuario,
                LocalDateTime.now().plusMinutes(10)
        );
        resetToken.setUsado(false);

        when(passwordResetTokenRepository.findByToken("token-usuario-inactivo")).thenReturn(Optional.of(resetToken));

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("token-usuario-inactivo", "otraPassword");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.resetPassword(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("La cuenta de usuario no está activa", ex.getReason());

        verify(usuarioRepository, never()).save(any(UsuarioEntity.class));
    }

    // --- Pruebas de B-17: Cambio de contraseña por usuario autenticado ---

    @Test
    @DisplayName("changePassword actualiza la contraseña con hash BCrypt y revoca tokens de sesión anteriores")
    public void testChangePassword_Success() {
        String email = "doctor@lumina.com";
        String claveActual = "ClaveVieja123!";
        String nuevaClave = "NuevaClaveSegura2026!";

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(20L);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(claveActual));
        usuario.setActivo(true);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO(claveActual, nuevaClave, nuevaClave);
        ChangePasswordResponseDTO response = authService.changePassword(email, request);

        assertNotNull(response);
        assertEquals("Contraseña actualizada exitosamente", response.getMensaje());

        // Comprobar que se guardó la nueva contraseña hasheada con BCrypt
        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());
        UsuarioEntity usuarioGuardado = captor.getValue();

        assertTrue(passwordEncoder.matches(nuevaClave, usuarioGuardado.getPasswordHash()));
        assertTrue(usuarioGuardado.getPasswordHash().startsWith("$2a$"));
        verify(refreshTokenService).revokeAllUserTokens(usuario);
    }

    @Test
    @DisplayName("changePassword falla con 400 Bad Request si la contraseña actual no coincide (Criterio 1)")
    public void testChangePassword_WrongCurrentPassword() {
        String email = "doctor@lumina.com";
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode("ClaveCorrecta123!"));
        usuario.setActivo(true);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO("ClaveErronea!", "NuevaClave2026!", "NuevaClave2026!");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.changePassword(email, request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("La contraseña actual no coincide", ex.getReason());

        verify(usuarioRepository, never()).save(any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("changePassword falla con 400 Bad Request si la confirmación no coincide")
    public void testChangePassword_ConfirmationMismatch() {
        String email = "doctor@lumina.com";
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode("ClaveActual123!"));
        usuario.setActivo(true);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO("ClaveActual123!", "NuevaClave2026!", "OtraClaveDiferente!");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.changePassword(email, request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("La confirmación de la contraseña no coincide", ex.getReason());

        verify(usuarioRepository, never()).save(any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("changePassword falla con 400 Bad Request si la nueva contraseña no cumple políticas de complejidad")
    public void testChangePassword_WeakComplexity() {
        String email = "doctor@lumina.com";
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode("ClaveActual123!"));
        usuario.setActivo(true);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        // Sin mayúsculas ni números
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO("ClaveActual123!", "solominusculas", "solominusculas");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.changePassword(email, request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("al menos 8 caracteres"));

        verify(usuarioRepository, never()).save(any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("changePassword falla con 401 Unauthorized si el usuario no está autenticado")
    public void testChangePassword_Unauthenticated() {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO("ClaveActual123!", "NuevaClave2026!", "NuevaClave2026!");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.changePassword("", request));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
