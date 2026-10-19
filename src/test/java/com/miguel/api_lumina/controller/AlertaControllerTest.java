package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.AlertaResponseDTO;
import com.miguel.api_lumina.exception.GlobalExceptionHandler;
import com.miguel.api_lumina.service.AlertaService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AlertaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AlertaService alertaService;

    @InjectMocks
    private AlertaController alertaController;

    private AlertaResponseDTO alertaDtoNoLeida;
    private AlertaResponseDTO alertaDtoLeida;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alertaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        alertaDtoNoLeida = new AlertaResponseDTO(
                1L, 50L, 10L, "Juan Perez",
                "Presion arterial critica", true, false, LocalDateTime.now()
        );

        alertaDtoLeida = new AlertaResponseDTO(
                2L, 50L, 10L, "Juan Perez",
                "Temperatura critica", true, true, LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("GET /api/alertas/medico/{id} sin filtro debe invocar servicio con leida null y retornar 200")
    void obtenerAlertas_sinFiltro_retorna200() throws Exception {
        when(alertaService.obtenerAlertasPorMedicoYEstado(eq(10L), isNull()))
                .thenReturn(List.of(alertaDtoNoLeida));

        mockMvc.perform(get("/api/alertas/medico/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].leida").value(false));
    }

    @Test
    @DisplayName("GET /api/alertas/medico/{id}?leida=true debe invocar servicio con leida true y retornar 200")
    void obtenerAlertas_conFiltroTrue_retorna200() throws Exception {
        when(alertaService.obtenerAlertasPorMedicoYEstado(10L, true))
                .thenReturn(List.of(alertaDtoLeida));

        mockMvc.perform(get("/api/alertas/medico/10")
                        .param("leida", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].leida").value(true));
    }

    @Test
    @DisplayName("PATCH /api/alertas/medico/{idMedico}/{idAlerta}/leida debe retornar 200 y DTO actualizado")
    void marcarAlertaComoLeida_valida_retorna200() throws Exception {
        when(alertaService.marcarAlertaComoLeida(10L, 1L))
                .thenReturn(alertaDtoLeida);

        mockMvc.perform(patch("/api/alertas/medico/10/1/leida")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.leida").value(true));
    }

    @Test
    @DisplayName("PATCH /api/alertas/medico/{idMedico}/{idAlerta}/leida debe responder 404 si la alerta no existe o pertenece a otro médico")
    void marcarAlertaComoLeida_inexistente_retorna404() throws Exception {
        when(alertaService.marcarAlertaComoLeida(10L, 999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta no encontrada"));

        mockMvc.perform(patch("/api/alertas/medico/10/999/leida")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
