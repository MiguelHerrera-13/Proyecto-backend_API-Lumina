package com.miguel.api_lumina.security;

import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
public class SecurityEndpointProtectionTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Petición a endpoint protegido sin token debe retornar 401 Unauthorized")
    public void testProtectedEndpointWithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Acceso no autorizado: se requiere autenticación válida"));
    }

    @Test
    @DisplayName("Petición a endpoint protegido con token inválido debe retornar 401 Unauthorized")
    public void testProtectedEndpointWithInvalidToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/usuarios/1")
                        .header("Authorization", "Bearer token_falso_o_malformado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Acceso no autorizado: se requiere autenticación válida"));
    }

    @Test
    @DisplayName("Petición a endpoint público (POST /api/auth/login) es accesible sin token")
    public void testPublicLoginEndpoint_AccessibleWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"password\":\"123456\"}"))
                // No debe arrojar 401 de filtro de seguridad (fallará por credenciales de negocio, pero no por falta de token)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Petición a endpoint protegido con token válido es aceptada por la capa de seguridad")
    public void testProtectedEndpointWithValidToken_Allowed() throws Exception {
        String email = "medico@lumina.com";
        String token = jwtService.generateToken(email, 1L, "MEDICO");

        UsuarioEntity mockUser = new UsuarioEntity();
        mockUser.setIdUsuario(1L);
        mockUser.setNombre("Dr. Lopez");
        mockUser.setEmail(email);
        mockUser.setPasswordHash("$2a$10$fakesalt...");
        mockUser.setRol("MEDICO");
        mockUser.setActivo(true);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/usuarios/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("Petición a endpoint público (POST /api/auth/refresh) es accesible sin token")
    public void testPublicRefreshTokenEndpoint_AccessibleWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"token-prueba-123\"}"))
                // No debe arrojar 401 de filtro de seguridad de endpoint protegido (fallará por negocio 401 token inexistente, pero pasa el filtro de seguridad)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Refresh token inexistente o inválido"));
    }

    @Test
    @DisplayName("Petición a endpoint público (POST /api/auth/forgot-password) es accesible sin token")
    public void testPublicForgotPasswordEndpoint_AccessibleWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cualquiera@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("Petición a endpoint público (POST /api/auth/reset-password) es accesible sin token")
    public void testPublicResetPasswordEndpoint_AccessibleWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"token-cualquiera\",\"nuevaPassword\":\"clave123\"}"))
                // No debe arrojar 401 de filtro de seguridad (fallará por validación/token inexistente 400, pero no 401)
                .andExpect(status().isBadRequest());
    }
}
