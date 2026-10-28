package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AuthResponseDTO;
import com.miguel.api_lumina.dto.RefreshTokenRequestDTO;
import com.miguel.api_lumina.entity.RefreshTokenEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.RefreshTokenRepository;
import com.miguel.api_lumina.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    public void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, jwtService);
        refreshTokenService.setRefreshExpirationDays(7);
    }

    @Test
    @DisplayName("createRefreshToken genera y persiste un nuevo refresh token UUID con expiración futura")
    public void testCreateRefreshToken_Success() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);
        usuario.setEmail("test@lumina.com");
        usuario.setActivo(true);

        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenEntity token = refreshTokenService.createRefreshToken(usuario);

        assertNotNull(token);
        assertNotNull(token.getToken());
        assertEquals(usuario, token.getUsuario());
        assertFalse(token.getRevocado());
        assertTrue(token.getFechaExpiracion().isAfter(LocalDateTime.now().plusDays(6)));

        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("refreshAccessToken rota el refresh token (revoca el anterior, genera uno nuevo) y emite un nuevo JWT")
    public void testRefreshAccessToken_Success_Rotation() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(2L);
        usuario.setEmail("doctor@lumina.com");
        usuario.setRol("MEDICO");
        usuario.setActivo(true);

        String oldTokenStr = "old-refresh-token-uuid-123";
        RefreshTokenEntity oldToken = new RefreshTokenEntity(oldTokenStr, usuario, LocalDateTime.now().plusDays(5));
        oldToken.setRevocado(false);

        when(refreshTokenRepository.findByToken(oldTokenStr)).thenReturn(Optional.of(oldToken));
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken("doctor@lumina.com", 2L, "MEDICO")).thenReturn("new.generated.jwt.token");

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO(oldTokenStr);
        AuthResponseDTO response = refreshTokenService.refreshAccessToken(request);

        assertNotNull(response);
        assertEquals("new.generated.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTipo());
        assertEquals("doctor@lumina.com", response.getEmail());
        assertEquals("MEDICO", response.getRol());
        assertNotNull(response.getRefreshToken());
        assertNotEquals(oldTokenStr, response.getRefreshToken());

        // Verificar que el token anterior quedó marcado como revocado
        assertTrue(oldToken.getRevocado());

        // Verificar que se guardó el token viejo revocado y el nuevo token activo
        verify(refreshTokenRepository, times(2)).save(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("refreshAccessToken falla con 401 Unauthorized si el refresh token no existe")
    public void testRefreshAccessToken_Failure_NotFound() {
        when(refreshTokenRepository.findByToken("inexistente-token")).thenReturn(Optional.empty());

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("inexistente-token");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> refreshTokenService.refreshAccessToken(request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Refresh token inexistente o inválido", ex.getReason());
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("refreshAccessToken falla con 401 Unauthorized si el refresh token ya fue revocado o usado")
    public void testRefreshAccessToken_Failure_AlreadyRevoked() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(3L);
        usuario.setEmail("revocado@lumina.com");
        usuario.setActivo(true);

        RefreshTokenEntity revokedToken = new RefreshTokenEntity("token-ya-revocado", usuario, LocalDateTime.now().plusDays(2));
        revokedToken.setRevocado(true);

        when(refreshTokenRepository.findByToken("token-ya-revocado")).thenReturn(Optional.of(revokedToken));

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("token-ya-revocado");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> refreshTokenService.refreshAccessToken(request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("El refresh token ya ha sido utilizado o revocado", ex.getReason());
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("refreshAccessToken falla con 401 Unauthorized si el refresh token está expirado y lo revoca")
    public void testRefreshAccessToken_Failure_Expired() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(4L);
        usuario.setEmail("expirado@lumina.com");
        usuario.setActivo(true);

        RefreshTokenEntity expiredToken = new RefreshTokenEntity("token-expirado", usuario, LocalDateTime.now().minusMinutes(5));
        expiredToken.setRevocado(false);

        when(refreshTokenRepository.findByToken("token-expirado")).thenReturn(Optional.of(expiredToken));

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("token-expirado");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> refreshTokenService.refreshAccessToken(request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("El refresh token ha expirado", ex.getReason());
        assertTrue(expiredToken.getRevocado());
        verify(refreshTokenRepository).save(expiredToken);
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("refreshAccessToken falla con 401 Unauthorized si el usuario asociado está inactivo")
    public void testRefreshAccessToken_Failure_InactiveUser() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(5L);
        usuario.setEmail("inactivo@lumina.com");
        usuario.setActivo(false);

        RefreshTokenEntity token = new RefreshTokenEntity("token-usuario-inactivo", usuario, LocalDateTime.now().plusDays(2));
        token.setRevocado(false);

        when(refreshTokenRepository.findByToken("token-usuario-inactivo")).thenReturn(Optional.of(token));

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("token-usuario-inactivo");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> refreshTokenService.refreshAccessToken(request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("La cuenta de usuario asociada no se encuentra activa", ex.getReason());
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("refreshAccessToken falla con 401 Unauthorized si el token recibido es nulo o en blanco")
    public void testRefreshAccessToken_Failure_BlankToken() {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("   ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> refreshTokenService.refreshAccessToken(request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Refresh token inexistente o inválido", ex.getReason());
    }
}
