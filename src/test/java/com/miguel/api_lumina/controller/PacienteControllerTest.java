package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.PacienteInactivoResponseDTO;
import com.miguel.api_lumina.dto.PacienteResponseDTO;
import com.miguel.api_lumina.exception.GlobalExceptionHandler;
import com.miguel.api_lumina.service.PacienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PacienteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PacienteService pacienteService;

    @InjectMocks
    private PacienteController pacienteController;

    private UsernamePasswordAuthenticationToken adminAuth;
    private UsernamePasswordAuthenticationToken cuidadorAuth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pacienteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        adminAuth = new UsernamePasswordAuthenticationToken(
                "admin@lumina.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        cuidadorAuth = new UsernamePasswordAuthenticationToken(
                "cuidador@lumina.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUIDADOR"))
        );
    }

    @Test
    @DisplayName("PATCH /api/pacientes/1/reactivar con rol ADMIN reactiva exitosamente (200 OK)")
    void reactivarPaciente_RolAdmin_Retorna200() throws Exception {
        PacienteResponseDTO response = new PacienteResponseDTO(
                1L, "Juan Pérez", LocalDate.of(1950, 1, 1), "Fase 1", true
        );

        when(pacienteService.reactivarPaciente(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/pacientes/1/reactivar")
                        .principal(adminAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPaciente").value(1L))
                .andExpect(jsonPath("$.nombreCompleto").value("Juan Pérez"))
                .andExpect(jsonPath("$.activo").value(true));

        verify(pacienteService).reactivarPaciente(1L);
    }

    @Test
    @DisplayName("PATCH /api/pacientes/1/reactivar con rol CUIDADOR es denegado (403 Forbidden)")
    void reactivarPaciente_RolCuidador_Retorna403() throws Exception {
        mockMvc.perform(patch("/api/pacientes/1/reactivar")
                        .principal(cuidadorAuth))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Solo usuarios con rol ADMIN pueden reactivar pacientes"));

        verify(pacienteService, never()).reactivarPaciente(anyLong());
    }

    @Test
    @DisplayName("PATCH /api/pacientes/1/reactivar sin autenticación retorna 401 Unauthorized")
    void reactivarPaciente_SinAuth_Retorna401() throws Exception {
        mockMvc.perform(patch("/api/pacientes/1/reactivar"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Acceso no autorizado"));

        verify(pacienteService, never()).reactivarPaciente(anyLong());
    }

    @Test
    @DisplayName("GET /api/pacientes/inactivos con rol ADMIN retorna lista de inactivos (200 OK)")
    void obtenerPacientesInactivos_RolAdmin_Retorna200() throws Exception {
        PacienteInactivoResponseDTO inactivo = new PacienteInactivoResponseDTO(
                2L, "María González", LocalDate.of(1945, 3, 12), "Fase 2",
                false, "Traslado de centro", LocalDateTime.now(), "admin@lumina.com"
        );

        when(pacienteService.obtenerPacientesInactivos()).thenReturn(List.of(inactivo));

        mockMvc.perform(get("/api/pacientes/inactivos")
                        .principal(adminAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPaciente").value(2L))
                .andExpect(jsonPath("$[0].nombreCompleto").value("María González"))
                .andExpect(jsonPath("$[0].activo").value(false))
                .andExpect(jsonPath("$[0].motivoBaja").value("Traslado de centro"))
                .andExpect(jsonPath("$[0].usuarioBaja").value("admin@lumina.com"));

        verify(pacienteService).obtenerPacientesInactivos();
    }

    @Test
    @DisplayName("GET /api/pacientes/inactivos con rol CUIDADOR es denegado (403 Forbidden)")
    void obtenerPacientesInactivos_RolCuidador_Retorna403() throws Exception {
        mockMvc.perform(get("/api/pacientes/inactivos")
                        .principal(cuidadorAuth))
                .andExpect(status().isForbidden());

        verify(pacienteService, never()).obtenerPacientesInactivos();
    }

    @Test
    @DisplayName("DELETE /api/pacientes/1 con motivo ejecuta baja lógica y retorna 204 No Content")
    void eliminarPaciente_ConMotivo_Retorna204() throws Exception {
        doNothing().when(pacienteService).darDeBajaPaciente(eq(1L), eq("Baja médica"), eq("admin@lumina.com"));

        mockMvc.perform(delete("/api/pacientes/1")
                        .param("motivo", "Baja médica")
                        .principal(adminAuth))
                .andExpect(status().isNoContent());

        verify(pacienteService).darDeBajaPaciente(1L, "Baja médica", "admin@lumina.com");
    }

    @Test
    @DisplayName("GET /api/pacientes/1 retorna 200 OK cuando existe")
    void obtenerPaciente_Existe_Retorna200() throws Exception {
        com.miguel.api_lumina.entity.PacienteEntity entity = new com.miguel.api_lumina.entity.PacienteEntity(
                1L, "Juan", LocalDate.of(1950, 1, 1), "Fase 1", true
        );

        when(pacienteService.obtenerPacientePorId(1L)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/api/pacientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPaciente").value(1L))
                .andExpect(jsonPath("$.nombreCompleto").value("Juan"));
    }

    @Test
    @DisplayName("GET /api/pacientes/99 retorna 404 Not Found cuando no existe")
    void obtenerPaciente_NoExiste_Retorna404() throws Exception {
        when(pacienteService.obtenerPacientePorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pacientes/99"))
                .andExpect(status().isNotFound());
    }
}
