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
import java.util.List;
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
        assertTrue(token.getFechaExpiracion().isBefore(LocalDateTime.now().plusDays(8)));
        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("refreshAccessToken valida vigencia, rota el refresh token y devuelve un nuevo JWT")
    public void testRefreshAccessToken_Success_RotatesToken() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(2L);
        usuario.setEmail("doctor@lumina.com");
        usuario.setRol("MEDICO");
        usuario.setActivo(true);

        RefreshTokenEntity oldToken = new RefreshTokenEntity("token-original-123", usuario, LocalDateTime.now().plusDays(5));
        oldToken.setRevocado(false);

        when(refreshTokenRepository.findByToken("token-original-123")).thenReturn(Optional.of(oldToken));
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken("doctor@lumina.com", 2L, "MEDICO")).thenReturn("nuevo-jwt-token-789");

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("token-original-123");
        AuthResponseDTO response = refreshTokenService.refreshAccessToken(request);

        assertNotNull(response);
        assertEquals("nuevo-jwt-token-789", response.getToken());
        assertEquals("Bearer", response.getTipo());
        assertEquals("doctor@lumina.com", response.getEmail());
        assertEquals("MEDICO", response.getRol());
        assertNotNull(response.getRefreshToken());
        assertNotEquals("token-original-123", response.getRefreshToken());

        // Comprobar que el token original quedó revocado
        assertTrue(oldToken.getRevocado());

        // Comprobar que el nuevo token se persistió con revocado = false
        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenRepository, times(2)).save(captor.capture());
        RefreshTokenEntity savedNewToken = captor.getAllValues().get(1);
        assertFalse(savedNewToken.getRevocado());
        assertEquals(usuario, savedNewToken.getUsuario());
    }

    @Test
    @DisplayName("refreshAccessToken falla con 401 Unauthorized si el token ya fue revocado (Ataque de repetición)")
    public void testRefreshAccessToken_AlreadyRevoked_ThrowsUnauthorized() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail("revocado@lumina.com");

        RefreshTokenEntity revokedToken = new RefreshTokenEntity("token-revocado-123", usuario, LocalDateTime.now().plusDays(5));
        revokedToken.setRevocado(true); // Ya consumido

        when(refreshTokenRepository.findByToken("token-revocado-123")).thenReturn(Optional.of(revokedToken));

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("token-revocado-123");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> refreshTokenService.refreshAccessToken(request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("El refresh token ya ha sido utilizado o revocado", ex.getReason());
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("refreshAccessToken falla con 401 Unauthorized si el token no existe en el repositorio")
    public void testRefreshAccessToken_NonExistent_ThrowsUnauthorized() {
        when(refreshTokenRepository.findByToken("token-fantasma")).thenReturn(Optional.empty());

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("token-fantasma");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> refreshTokenService.refreshAccessToken(request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Refresh token inexistente o inválido", ex.getReason());
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("refreshAccessToken falla con 401 Unauthorized si el token está expirado y lo marca como revocado")
    public void testRefreshAccessToken_Expired_ThrowsUnauthorized() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail("expirado@lumina.com");

        RefreshTokenEntity expiredToken = new RefreshTokenEntity("token-expirado", usuario, LocalDateTime.now().minusDays(1));
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

    @Test
    @DisplayName("revokeRefreshToken marca el token como revocado si existe en la base de datos")
    public void testRevokeRefreshToken_Success() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail("test@lumina.com");

        RefreshTokenEntity token = new RefreshTokenEntity("token-a-revocar", usuario, LocalDateTime.now().plusDays(5));
        token.setRevocado(false);

        when(refreshTokenRepository.findByToken("token-a-revocar")).thenReturn(Optional.of(token));

        refreshTokenService.revokeRefreshToken("token-a-revocar");

        assertTrue(token.getRevocado());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    @DisplayName("revokeAllUserTokens marca como revocados todos los tokens activos del usuario")
    public void testRevokeAllUserTokens_Success() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail("test@lumina.com");

        RefreshTokenEntity t1 = new RefreshTokenEntity("t1", usuario, LocalDateTime.now().plusDays(5));
        t1.setRevocado(false);
        RefreshTokenEntity t2 = new RefreshTokenEntity("t2", usuario, LocalDateTime.now().plusDays(3));
        t2.setRevocado(false);

        when(refreshTokenRepository.findByUsuarioAndRevocadoFalse(usuario)).thenReturn(List.of(t1, t2));

        refreshTokenService.revokeAllUserTokens(usuario);

        assertTrue(t1.getRevocado());
        assertTrue(t2.getRevocado());
        verify(refreshTokenRepository).saveAll(List.of(t1, t2));
    }
}
