package com.miguel.api_lumina.service;

import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginAttemptServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    public void setUp() {
        loginAttemptService = new LoginAttemptService(usuarioRepository);
    }

    @Test
    @DisplayName("Primer intento fallido incrementa contador a 1 sin bloquear")
    public void testRegistrarIntentoFallido_PrimerIntento() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIntentosFallidos(0);

        loginAttemptService.registrarIntentoFallido(usuario);

        assertEquals(1, usuario.getIntentosFallidos());
        assertNull(usuario.getBloqueadoHasta());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Al alcanzar 5 intentos fallidos se bloquea la cuenta por 15 minutos")
    public void testRegistrarIntentoFallido_QuintoIntentoBloqueaCuenta() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIntentosFallidos(4);

        LocalDateTime antes = LocalDateTime.now().plusMinutes(14);
        loginAttemptService.registrarIntentoFallido(usuario);

        assertEquals(5, usuario.getIntentosFallidos());
        assertNotNull(usuario.getBloqueadoHasta());
        assertTrue(usuario.getBloqueadoHasta().isAfter(antes));
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Resetear intentos vuelve el contador a 0 y limpia el bloqueo")
    public void testResetearIntentos() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIntentosFallidos(5);
        usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(15));

        loginAttemptService.resetearIntentos(usuario);

        assertEquals(0, usuario.getIntentosFallidos());
        assertNull(usuario.getBloqueadoHasta());
        verify(usuarioRepository, times(1)).save(usuario);
    }
}
