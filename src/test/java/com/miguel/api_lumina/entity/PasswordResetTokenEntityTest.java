package com.miguel.api_lumina.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordResetTokenEntityTest {

    @Test
    @DisplayName("isExpirado retorna false si fechaExpiracion está en el futuro y true si está en el pasado")
    public void testIsExpirado() {
        UsuarioEntity usuario = new UsuarioEntity();
        PasswordResetTokenEntity token = new PasswordResetTokenEntity("token-123", usuario, LocalDateTime.now().plusMinutes(15));

        assertFalse(token.isExpirado());

        token.setFechaExpiracion(LocalDateTime.now().minusSeconds(1));
        assertTrue(token.isExpirado());
    }

    @Test
    @DisplayName("isValido retorna true solo si no fue usado y no está expirado")
    public void testIsValido() {
        UsuarioEntity usuario = new UsuarioEntity();
        PasswordResetTokenEntity token = new PasswordResetTokenEntity("token-123", usuario, LocalDateTime.now().plusMinutes(15));

        assertTrue(token.isValido());

        // Si fue usado deja de ser válido
        token.setUsado(true);
        assertFalse(token.isValido());

        // Si no fue usado pero está expirado, tampoco es válido
        token.setUsado(false);
        token.setFechaExpiracion(LocalDateTime.now().minusMinutes(5));
        assertFalse(token.isValido());
    }

    @Test
    @DisplayName("Setters y Getters de PasswordResetTokenEntity funcionan correctamente")
    public void testGettersAndSetters() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);

        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setIdToken(100L);
        token.setToken("token-xyz");
        token.setUsuario(usuario);
        token.setUsado(false);

        LocalDateTime ahora = LocalDateTime.now();
        token.setFechaCreacion(ahora);
        token.setFechaExpiracion(ahora.plusMinutes(20));

        assertEquals(100L, token.getIdToken());
        assertEquals("token-xyz", token.getToken());
        assertEquals(usuario, token.getUsuario());
        assertFalse(token.getUsado());
        assertEquals(ahora, token.getFechaCreacion());
        assertEquals(ahora.plusMinutes(20), token.getFechaExpiracion());
    }
}
