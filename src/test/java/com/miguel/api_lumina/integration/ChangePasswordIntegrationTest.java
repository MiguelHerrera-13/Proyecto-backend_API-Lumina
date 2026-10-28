package com.miguel.api_lumina.integration;

import tools.jackson.databind.ObjectMapper;
import com.miguel.api_lumina.dto.ChangePasswordRequestDTO;
import com.miguel.api_lumina.dto.LoginRequestDTO;
import com.miguel.api_lumina.entity.UsuarioEntity;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ChangePasswordIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

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
    @DisplayName("Flujo E2E Cambio de Contraseña: Login -> Validaciones 400 -> Cambio Exitoso -> Login con nueva contraseña")
    public void testFullChangePasswordFlow_E2E() throws Exception {
        String email = "changepass_e2e_" + System.currentTimeMillis() + "@lumina.com";
        String passwordOriginal = "ClaveOriginal123!";
        String nuevaPasswordValida = "NuevaClaveSegura2026!";

        // 1. Crear usuario de prueba
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNombre("Dr. Cambio Clave");
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(passwordOriginal));
        usuario.setRol("MEDICO");
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);

        // 2. Iniciar sesión y obtener JWT inicial
        LoginRequestDTO loginRequest = new LoginRequestDTO(email, passwordOriginal);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        var loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String jwtToken = loginJson.get("token").asText();

        // 3. Intento sin token de autenticación -> debe retornar 401 Unauthorized
        ChangePasswordRequestDTO sinAuthRequest = new ChangePasswordRequestDTO(passwordOriginal, nuevaPasswordValida, nuevaPasswordValida);
        mockMvc.perform(put("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sinAuthRequest)))
                .andExpect(status().isUnauthorized());

        // 4. Intento con contraseña actual errónea -> Criterio de aceptación 1: 400 Bad Request
        ChangePasswordRequestDTO claveErroneaRequest = new ChangePasswordRequestDTO("ClaveErronea999!", nuevaPasswordValida, nuevaPasswordValida);
        mockMvc.perform(put("/api/auth/change-password")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claveErroneaRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La contraseña actual no coincide"));

        // 5. Intento con confirmación que no coincide -> 400 Bad Request
        ChangePasswordRequestDTO confirmacionMismatchRequest = new ChangePasswordRequestDTO(passwordOriginal, nuevaPasswordValida, "NoCoincide2026!");
        mockMvc.perform(put("/api/auth/change-password")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmacionMismatchRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La confirmación de la contraseña no coincide"));

        // 6. Intento con nueva contraseña que no cumple complejidad (sin números ni mayúsculas, < 8 caracteres) -> 400 Bad Request
        ChangePasswordRequestDTO debilRequest = new ChangePasswordRequestDTO(passwordOriginal, "simple", "simple");
        mockMvc.perform(put("/api/auth/change-password")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(debilRequest)))
                .andExpect(status().isBadRequest());

        // 7. Cambio exitoso con datos válidos -> 200 OK
        ChangePasswordRequestDTO exitoRequest = new ChangePasswordRequestDTO(passwordOriginal, nuevaPasswordValida, nuevaPasswordValida);
        mockMvc.perform(put("/api/auth/change-password")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exitoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Contraseña actualizada exitosamente"));

        // 8. Criterio de aceptación 2: Verificar en BD que la contraseña esté hasheada con BCrypt
        UsuarioEntity usuarioActualizado = usuarioRepository.findByEmail(email).orElseThrow();
        assertTrue(usuarioActualizado.getPasswordHash().startsWith("$2a$"), "El hash debe tener formato BCrypt ($2a$)");
        assertTrue(passwordEncoder.matches(nuevaPasswordValida, usuarioActualizado.getPasswordHash()), "El nuevo hash debe verificar contra la nueva contraseña");

        // 9. Comprobar que la contraseña anterior ya no funciona
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO(email, passwordOriginal))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));

        // 10. Comprobar que la nueva contraseña permite autenticarse exitosamente
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO(email, nuevaPasswordValida))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
