package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.AlergiaResponseDTO;
import com.miguel.api_lumina.exception.GlobalExceptionHandler;
import com.miguel.api_lumina.service.AlergiaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AlergiaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AlergiaService alergiaService;

    @InjectMocks
    private AlergiaController alergiaController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(alergiaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/pacientes/{id}/alergias con datos válidos retorna 201 Created")
    public void testCrearAlergia_Valido_Retorna201() throws Exception {
        AlergiaResponseDTO responseDTO = new AlergiaResponseDTO(
                1L, 1L, "Penicilina", "GRAVE", "Anafilaxia", true
        );

        when(alergiaService.crearAlergia(eq(1L), any())).thenReturn(responseDTO);

        String jsonRequest = """
                {
                    "tipo": "Penicilina",
                    "severidad": "GRAVE",
                    "descripcion": "Anafilaxia"
                }
                """;

        mockMvc.perform(post("/api/pacientes/1/alergias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAlergia").value(1))
                .andExpect(jsonPath("$.tipo").value("Penicilina"))
                .andExpect(jsonPath("$.severidad").value("GRAVE"))
                .andExpect(jsonPath("$.descripcion").value("Anafilaxia"));
    }

    @Test
    @DisplayName("POST /api/pacientes/{id}/alergias con severidad inválida retorna 400 Bad Request")
    public void testCrearAlergia_SeveridadInvalida_Retorna400() throws Exception {
        String jsonRequest = """
                {
                    "tipo": "Penicilina",
                    "severidad": "CRITICA",
                    "descripcion": "Anafilaxia"
                }
                """;

        mockMvc.perform(post("/api/pacientes/1/alergias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.severidad").exists());
    }

    @Test
    @DisplayName("POST /api/pacientes/{id}/alergias con tipo duplicado retorna 409 Conflict")
    public void testCrearAlergia_Duplicada_Retorna409() throws Exception {
        when(alergiaService.crearAlergia(eq(1L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una alergia activa"));

        String jsonRequest = """
                {
                    "tipo": "Penicilina",
                    "severidad": "LEVE",
                    "descripcion": "Picazón"
                }
                """;

        mockMvc.perform(post("/api/pacientes/1/alergias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ya existe una alergia activa"));
    }

    @Test
    @DisplayName("GET /api/pacientes/{id}/alergias retorna 200 OK con lista completa")
    public void testListarAlergiasPorPaciente_Retorna200() throws Exception {
        AlergiaResponseDTO a1 = new AlergiaResponseDTO(1L, 1L, "Polen", "LEVE", "Rinitis", true);
        AlergiaResponseDTO a2 = new AlergiaResponseDTO(2L, 1L, "Aspirina", "MODERADA", "Urticaria", true);

        when(alergiaService.listarAlergiasPorPaciente(1L)).thenReturn(List.of(a1, a2));

        mockMvc.perform(get("/api/pacientes/1/alergias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipo").value("Polen"))
                .andExpect(jsonPath("$[1].tipo").value("Aspirina"));
    }

    @Test
    @DisplayName("GET /api/pacientes/{id}/alergias/{idAlergia} retorna 200 OK")
    public void testObtenerAlergiaPorId_Retorna200() throws Exception {
        AlergiaResponseDTO a1 = new AlergiaResponseDTO(1L, 1L, "Polen", "LEVE", "Rinitis", true);

        when(alergiaService.obtenerAlergiaPorId(1L, 1L)).thenReturn(a1);

        mockMvc.perform(get("/api/pacientes/1/alergias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAlergia").value(1))
                .andExpect(jsonPath("$.tipo").value("Polen"));
    }

    @Test
    @DisplayName("PUT /api/pacientes/{id}/alergias/{idAlergia} actualiza y retorna 200 OK")
    public void testActualizarAlergia_Retorna200() throws Exception {
        AlergiaResponseDTO actualizado = new AlergiaResponseDTO(1L, 1L, "Polen", "MODERADA", "Rinitis severa", true);

        when(alergiaService.actualizarAlergia(eq(1L), eq(1L), any())).thenReturn(actualizado);

        String jsonRequest = """
                {
                    "tipo": "Polen",
                    "severidad": "MODERADA",
                    "descripcion": "Rinitis severa"
                }
                """;

        mockMvc.perform(put("/api/pacientes/1/alergias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.severidad").value("MODERADA"));
    }

    @Test
    @DisplayName("DELETE /api/pacientes/{id}/alergias/{idAlergia} retorna 204 No Content")
    public void testEliminarAlergia_Retorna204() throws Exception {
        doNothing().when(alergiaService).eliminarAlergia(1L, 1L);

        mockMvc.perform(delete("/api/pacientes/1/alergias/1"))
                .andExpect(status().isNoContent());

        verify(alergiaService, times(1)).eliminarAlergia(1L, 1L);
    }
}
