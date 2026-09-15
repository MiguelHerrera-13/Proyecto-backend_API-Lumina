package com.miguel.api_lumina.controller;

import tools.jackson.databind.ObjectMapper;
import com.miguel.api_lumina.dto.UsuarioRequestDTO;
import com.miguel.api_lumina.dto.UsuarioResponseDTO;
import com.miguel.api_lumina.exception.EmailYaRegistradoException;
import com.miguel.api_lumina.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
public class UsuarioControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UsuarioController(usuarioService))
                .setControllerAdvice(new com.miguel.api_lumina.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    public void registrarUsuarioValido_DebeRetornar201() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setNombre("Miguel");
        request.setEmail("miguel@test.com");
        request.setPassword("123456");
        request.setRol("ADMIN");

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setIdUsuario(1L);
        response.setNombre("Miguel");
        response.setEmail("miguel@test.com");
        response.setRol("ADMIN");
        response.setActivo(true);

        when(usuarioService.registrarUsuario(any(UsuarioRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").value(1L));
    }

    @Test
    public void registrarUsuarioEmailRepetido_DebeRetornar409() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setNombre("Juan");
        request.setEmail("duplicado@test.com");
        request.setPassword("123456");
        request.setRol("MEDICO");

        when(usuarioService.registrarUsuario(any(UsuarioRequestDTO.class)))
                .thenThrow(new EmailYaRegistradoException("El email ya se encuentra registrado"));

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    public void registrarUsuarioRolInvalido_DebeRetornar400() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setNombre("Carlos");
        request.setEmail("carlos@test.com");
        request.setPassword("123456");
        request.setRol("INVALID_ROLE");

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.rol").exists());
    }

    @Test
    public void obtenerUsuarioInexistente_DebeRetornar404() throws Exception {
        when(usuarioService.obtenerUsuarioPorId(eq(99L))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound());
    }
}
