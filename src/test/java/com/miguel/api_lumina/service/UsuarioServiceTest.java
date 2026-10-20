package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.UsuarioRequestDTO;
import com.miguel.api_lumina.dto.UsuarioResponseDTO;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.exception.EmailYaRegistradoException;
import com.miguel.api_lumina.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private PasswordEncoder passwordEncoder;
    private UsuarioService usuarioService;

    @BeforeEach
    public void setUp() {
        passwordEncoder = spy(new BCryptPasswordEncoder());
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Registrar usuario nuevo debe cifrar la contraseña con BCrypt y nunca guardarla en texto plano")
    public void testRegistrarUsuario_HashesPasswordWithBcrypt() {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setNombre("Carlos Medico");
        request.setEmail("carlos@lumina.com");
        request.setPassword("PasswordSegura2026");
        request.setRol("MEDICO");

        when(usuarioRepository.existsByEmail("carlos@lumina.com")).thenReturn(false);
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(invocation -> {
            UsuarioEntity entity = invocation.getArgument(0);
            entity.setIdUsuario(100L);
            return entity;
        });

        UsuarioResponseDTO response = usuarioService.registrarUsuario(request);

        // Verificar que la contraseña cruda NUNCA se guardó
        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());
        UsuarioEntity savedEntity = captor.getValue();

        assertNotEquals("PasswordSegura2026", savedEntity.getPasswordHash());
        assertTrue(passwordEncoder.matches("PasswordSegura2026", savedEntity.getPasswordHash()));
        assertTrue(savedEntity.getPasswordHash().startsWith("$2a$"));
        verify(passwordEncoder).encode("PasswordSegura2026");

        // Verificar que el response DTO no contenga la contraseña ni el hash
        assertNotNull(response);
        assertEquals(100L, response.getIdUsuario());
        assertEquals("Carlos Medico", response.getNombre());
        assertEquals("carlos@lumina.com", response.getEmail());
        assertEquals("MEDICO", response.getRol());
        assertTrue(response.getActivo());
    }

    @Test
    @DisplayName("Registrar usuario con email duplicado debe lanzar EmailYaRegistradoException")
    public void testRegistrarUsuario_EmailDuplicado() {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setEmail("duplicado@lumina.com");
        request.setPassword("123456");

        when(usuarioRepository.existsByEmail("duplicado@lumina.com")).thenReturn(true);

        assertThrows(EmailYaRegistradoException.class, () -> usuarioService.registrarUsuario(request));
        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Dar de baja usuario debe marcar activo=false")
    public void testDarDeBajaUsuario() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(50L);
        usuario.setActivo(true);

        when(usuarioRepository.findById(50L)).thenReturn(Optional.of(usuario));

        usuarioService.darDeBajaUsuario(50L);

        assertFalse(usuario.getActivo());
        verify(usuarioRepository).save(usuario);
    }
}
