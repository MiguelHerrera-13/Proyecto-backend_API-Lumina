package com.miguel.api_lumina.integration;

import tools.jackson.databind.ObjectMapper;
import com.miguel.api_lumina.dto.ForgotPasswordRequestDTO;
import com.miguel.api_lumina.dto.LoginRequestDTO;
import com.miguel.api_lumina.dto.ResetPasswordRequestDTO;
import com.miguel.api_lumina.entity.PasswordResetTokenEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.PasswordResetTokenRepository;
import com.miguel.api_lumina.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class PasswordResetIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Flujo completo E2E: forgot-password -> reset-password con BCrypt -> login con nueva contraseña -> token de un solo uso")
    public void testFullPasswordResetFlow_E2E() throws Exception {
        String email = "recuperacion_e2e_" + System.currentTimeMillis() + "@lumina.com";
        String passwordInicial = "claveInicial123";
        String passwordNueva = "miNuevaPasswordSegura2026!";

        // 1. Crear usuario de prueba
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNombre("Usuario Recuperación");
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(passwordInicial));
        usuario.setRol("MEDICO");
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);

        // 2. Solicitar recuperación de contraseña (POST /api/auth/forgot-password)
        ForgotPasswordRequestDTO forgotRequest = new ForgotPasswordRequestDTO(email);
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Si el correo electrónico está registrado, recibirás un enlace o token para restablecer tu contraseña."));

        // 3. Verificar que se persistió el token en la base de datos
        var tokens = passwordResetTokenRepository.findByUsuarioAndUsadoFalse(usuario);
        assertFalse(tokens.isEmpty(), "Debe haberse generado un token para el usuario");
        PasswordResetTokenEntity tokenEntity = tokens.get(0);
        String tokenGenerado = tokenEntity.getToken();
        assertNotNull(tokenGenerado);
        assertFalse(tokenEntity.getUsado());
        assertTrue(tokenEntity.getFechaExpiracion().isAfter(LocalDateTime.now().plusMinutes(10)));

        // 4. Restablecer contraseña con el token (POST /api/auth/reset-password)
        ResetPasswordRequestDTO resetRequest = new ResetPasswordRequestDTO(tokenGenerado, passwordNueva);
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Contraseña restablecida exitosamente"));

        // 5. Verificar que el token quedó marcado como usado (un solo uso)
        Optional<PasswordResetTokenEntity> tokenActualizado = passwordResetTokenRepository.findByToken(tokenGenerado);
        assertTrue(tokenActualizado.isPresent());
        assertTrue(tokenActualizado.get().getUsado());

        // 6. Verificar que intentar reutilizar el token falla con 400 Bad Request
        ResetPasswordRequestDTO reintentoRequest = new ResetPasswordRequestDTO(tokenGenerado, "otraClaveMas");
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reintentoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El token de recuperación ya ha sido utilizado"));

        // 7. Verificar que el usuario ahora puede iniciar sesión con la nueva contraseña
        LoginRequestDTO loginNuevo = new LoginRequestDTO(email, passwordNueva);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginNuevo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.email").value(email));

        // 8. Verificar que el login con la contraseña antigua falla con 401 Unauthorized
        LoginRequestDTO loginAntiguo = new LoginRequestDTO(email, passwordInicial);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAntiguo)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Anti-enumeración: POST /api/auth/forgot-password con email inexistente retorna 200 idéntico")
    public void testForgotPassword_NonExistentEmail_AntiEnumeration() throws Exception {
        ForgotPasswordRequestDTO forgotRequest = new ForgotPasswordRequestDTO("totalmente_inexistente_9999@lumina.com");
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Si el correo electrónico está registrado, recibirás un enlace o token para restablecer tu contraseña."));
    }

    @Test
    @DisplayName("Vencimiento estricto: POST /api/auth/reset-password falla si el token está expirado")
    public void testResetPassword_ExpiredToken_Returns400() throws Exception {
        String email = "expirado_" + System.currentTimeMillis() + "@lumina.com";

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNombre("Usuario Expirado");
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode("clave123"));
        usuario.setRol("CUIDADOR");
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);

        // Crear token expirado manualmente
        PasswordResetTokenEntity tokenExpirado = new PasswordResetTokenEntity(
                "token-ya-vencido-" + System.currentTimeMillis(),
                usuario,
                LocalDateTime.now().minusMinutes(5)
        );
        passwordResetTokenRepository.save(tokenExpirado);

        ResetPasswordRequestDTO resetRequest = new ResetPasswordRequestDTO(tokenExpirado.getToken(), "claveNueva");
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El token de recuperación ha expirado"));
    }
}
