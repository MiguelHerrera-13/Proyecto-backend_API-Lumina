package com.miguel.api_lumina.integration;

import tools.jackson.databind.ObjectMapper;
import com.miguel.api_lumina.dto.LoginRequestDTO;
import com.miguel.api_lumina.dto.LogoutRequestDTO;
import com.miguel.api_lumina.dto.RefreshTokenRequestDTO;
import com.miguel.api_lumina.entity.RefreshTokenEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.RefreshTokenRepository;
import com.miguel.api_lumina.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class LogoutIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

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
    @DisplayName("Flujo E2E Logout: Login -> Acceso permitido -> Logout -> Refresh token revocado -> Access token en blacklist")
    public void testFullLogoutFlow_E2E() throws Exception {
        String email = "logout_e2e_" + System.currentTimeMillis() + "@lumina.com";
        String password = "claveSegura123";

        // 1. Crear usuario de prueba
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNombre("Dr. Desconexión");
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setRol("MEDICO");
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);

        // 2. Iniciar sesión y obtener JWT y Refresh Token
        LoginRequestDTO loginRequest = new LoginRequestDTO(email, password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        var loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String jwtToken = loginJson.get("token").asText();
        String refreshToken = loginJson.get("refreshToken").asText();

        assertNotNull(jwtToken);
        assertNotNull(refreshToken);

        // 3. Comprobar que el usuario puede acceder a un endpoint protegido con el JWT recién emitido
        mockMvc.perform(get("/api/usuarios/" + usuario.getIdUsuario())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        // 4. Ejecutar Logout explícito enviando el JWT en la cabecera y el refresh token en el cuerpo
        LogoutRequestDTO logoutRequest = new LogoutRequestDTO(refreshToken);
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Sesión cerrada exitosamente"));

        // 5. Criterio de aceptación: Tras el logout, el refresh token quedó revocado en la base de datos
        Optional<RefreshTokenEntity> tokenEntityOpt = refreshTokenRepository.findByToken(refreshToken);
        assertTrue(tokenEntityOpt.isPresent(), "El refresh token debe existir en la base de datos");
        assertTrue(tokenEntityOpt.get().getRevocado(), "El refresh token debe haber sido revocado en BD");

        // 6. Criterio de aceptación: Tras el logout, el refresh token NO puede ser reutilizado en /api/auth/refresh
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO(refreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("El refresh token ya ha sido utilizado o revocado"));

        // 7. Criterio de aceptación: El access token JWT quedó en lista negra y ya no permite acceder a endpoints protegidos
        mockMvc.perform(get("/api/usuarios/" + usuario.getIdUsuario())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Acceso no autorizado: se requiere autenticación válida"));
    }
}
