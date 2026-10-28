package com.miguel.api_lumina.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class RefreshTokenEntityTest {

    @Test
    @DisplayName("isExpirado retorna false si fechaExpiracion está en el futuro y true si está en el pasado")
    public void testIsExpirado() {
        UsuarioEntity usuario = new UsuarioEntity();
        RefreshTokenEntity token = new RefreshTokenEntity("token-123", usuario, LocalDateTime.now().plusDays(7));

        assertFalse(token.isExpirado());

        token.setFechaExpiracion(LocalDateTime.now().minusSeconds(1));
        assertTrue(token.isExpirado());
    }

    @Test
    @DisplayName("isRevocado retorna el estado del booleano revocado")
    public void testIsRevocado() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setRevocado(false);
        assertFalse(token.isRevocado());

        token.setRevocado(true);
        assertTrue(token.isRevocado());
    }

    @Test
    @DisplayName("isValido retorna true solo si no fue revocado y no está expirado")
    public void testIsValido() {
        UsuarioEntity usuario = new UsuarioEntity();
        RefreshTokenEntity token = new RefreshTokenEntity("token-123", usuario, LocalDateTime.now().plusDays(7));

        assertTrue(token.isValido());

        // Si fue revocado, no es válido
        token.setRevocado(true);
        assertFalse(token.isValido());

        // Si no está revocado pero está expirado, tampoco es válido
        token.setRevocado(false);
        token.setFechaExpiracion(LocalDateTime.now().minusHours(1));
        assertFalse(token.isValido());
    }

    @Test
    @DisplayName("Setters y Getters de RefreshTokenEntity funcionan correctamente")
    public void testGettersAndSetters() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);

        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setIdRefreshToken(10L);
        token.setToken("uuid-token-xyz");
        token.setUsuario(usuario);
        token.setRevocado(false);

        LocalDateTime ahora = LocalDateTime.now();
        token.setFechaCreacion(ahora);
        token.setFechaExpiracion(ahora.plusDays(7));

        assertEquals(10L, token.getIdRefreshToken());
        assertEquals("uuid-token-xyz", token.getToken());
        assertEquals(usuario, token.getUsuario());
        assertFalse(token.getRevocado());
        assertEquals(ahora, token.getFechaCreacion());
        assertEquals(ahora.plusDays(7), token.getFechaExpiracion());
    }
}
