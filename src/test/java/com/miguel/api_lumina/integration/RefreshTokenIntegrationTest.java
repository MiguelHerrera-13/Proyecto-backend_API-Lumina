package com.miguel.api_lumina.integration;

import tools.jackson.databind.ObjectMapper;
import com.miguel.api_lumina.dto.LoginRequestDTO;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class RefreshTokenIntegrationTest {

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
    @DisplayName("Flujo E2E: Login -> Emisión Refresh Token -> Refresh con rotación -> Detección de replay attack")
    public void testFullRefreshTokenFlow_E2E() throws Exception {
        String email = "refresh_e2e_" + System.currentTimeMillis() + "@lumina.com";
        String password = "claveSegura123";

        // 1. Crear usuario de prueba
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNombre("Dr. Rotación");
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setRol("MEDICO");
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        // 2. Login y obtención del primer refresh token (T1)
        LoginRequestDTO loginRequest = new LoginRequestDTO(email, password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        var jsonNode = objectMapper.readTree(responseBody);
        String token1 = jsonNode.get("refreshToken").asText();
        String jwt1 = jsonNode.get("token").asText();

        assertNotNull(token1);
        assertNotNull(jwt1);

        // 3. Renovar token de acceso usando el primer refresh token (T1)
        RefreshTokenRequestDTO refreshRequest1 = new RefreshTokenRequestDTO(token1);
        MvcResult refreshResult1 = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        var refreshJsonNode1 = objectMapper.readTree(refreshResult1.getResponse().getContentAsString());
        String token2 = refreshJsonNode1.get("refreshToken").asText();
        String jwt2 = refreshJsonNode1.get("token").asText();

        // Verificar rotación: T2 debe ser diferente a T1
        assertNotEquals(token1, token2);

        // 4. Verificar en base de datos que T1 quedó revocado y T2 está activo
        Optional<RefreshTokenEntity> entityT1 = refreshTokenRepository.findByToken(token1);
        assertTrue(entityT1.isPresent());
        assertTrue(entityT1.get().getRevocado(), "El token anterior T1 debe estar marcado como revocado");

        Optional<RefreshTokenEntity> entityT2 = refreshTokenRepository.findByToken(token2);
        assertTrue(entityT2.isPresent());
        assertFalse(entityT2.get().getRevocado(), "El nuevo token T2 debe estar activo (no revocado)");

        // 5. Intento de reuso de T1 (Mitigación de ataques de repetición) -> debe fallar con 401 Unauthorized
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO(token1))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("El refresh token ya ha sido utilizado o revocado"));

        // 6. Renovar ahora usando el nuevo token T2 -> debe rotar a T3 exitosamente
        RefreshTokenRequestDTO refreshRequest2 = new RefreshTokenRequestDTO(token2);
        MvcResult refreshResult2 = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        var refreshJsonNode2 = objectMapper.readTree(refreshResult2.getResponse().getContentAsString());
        String token3 = refreshJsonNode2.get("refreshToken").asText();

        assertNotEquals(token2, token3);
    }

    @Test
    @DisplayName("POST /api/auth/refresh con refresh token expirado devuelve 401 Unauthorized")
    public void testRefreshToken_Expired_Returns401() throws Exception {
        String email = "expirado_e2e_" + System.currentTimeMillis() + "@lumina.com";

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNombre("Usuario Expirado");
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode("clave123"));
        usuario.setRol("CUIDADOR");
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);

        // Crear token expirado directamente en BD
        RefreshTokenEntity expiredToken = new RefreshTokenEntity(
                "token-vencido-" + System.currentTimeMillis(),
                usuario,
                LocalDateTime.now().minusDays(1)
        );
        refreshTokenRepository.save(expiredToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO(expiredToken.getToken()))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("El refresh token ha expirado"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh con refresh token inexistente devuelve 401 Unauthorized")
    public void testRefreshToken_NonExistent_Returns401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequestDTO("token-fantasma-99999"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Refresh token inexistente o inválido"));
    }
}
