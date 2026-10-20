package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AuthResponseDTO;
import com.miguel.api_lumina.dto.LoginRequestDTO;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.UsuarioRepository;
import com.miguel.api_lumina.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(usuarioRepository, passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("Login exitoso con contraseña almacenada en formato BCrypt")
    public void testLogin_Success_BcryptPassword() {
        String rawPassword = "passwordSegura123";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);
        usuario.setEmail("doctor@test.com");
        usuario.setPasswordHash(hashedPassword);
        usuario.setRol("MEDICO");
        usuario.setActivo(true);

        when(usuarioRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken("doctor@test.com", 1L, "MEDICO")).thenReturn("fake.jwt.token");

        LoginRequestDTO request = new LoginRequestDTO("doctor@test.com", rawPassword);
        AuthResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTipo());
        assertEquals("doctor@test.com", response.getEmail());
        assertEquals("MEDICO", response.getRol());

        // No debe intentar guardar si ya era BCrypt
        verify(usuarioRepository, never()).save(any(UsuarioEntity.class));
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

        when(usuarioRepository.findByEmail("legacy@test.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken("legacy@test.com", 2L, "CUIDADOR")).thenReturn("fake.jwt.migrated");

        LoginRequestDTO request = new LoginRequestDTO("legacy@test.com", legacyPassword);
        AuthResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake.jwt.migrated", response.getToken());

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
    }

    @Test
    @DisplayName("Login fallido con email inexistente")
    public void testLogin_Failure_UserNotFound() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        LoginRequestDTO request = new LoginRequestDTO("noexiste@test.com", "algunaClave");

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any(), any(), any());
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
    }
}
