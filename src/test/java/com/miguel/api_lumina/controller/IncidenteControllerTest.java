package com.miguel.api_lumina.controller;

import tools.jackson.databind.ObjectMapper;
import com.miguel.api_lumina.dto.IncidenteRequestDTO;
import com.miguel.api_lumina.dto.IncidenteResponseDTO;
import com.miguel.api_lumina.exception.GlobalExceptionHandler;
import com.miguel.api_lumina.service.IncidenteService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class IncidenteControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IncidenteService incidenteService;

    @InjectMocks
    private IncidenteController incidenteController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(incidenteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/reportes-diarios/1/incidentes con datos válidos retorna 201 Created")
    void registrarIncidente_Valido_Retorna201() throws Exception {
        String json = "{\"tipoIncidente\":\"CAIDA\",\"descripcion\":\"Paciente cayó al resbalar en el baño.\",\"severidad\":\"ALTA\"}";

        IncidenteResponseDTO response = new IncidenteResponseDTO(
                1L,
                1L,
                "CAIDA",
                "Paciente cayó al resbalar en el baño.",
                "ALTA",
                LocalDateTime.now(),
                true,
                true
        );

        when(incidenteService.registrarIncidente(eq(1L), any(IncidenteRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/reportes-diarios/1/incidentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idIncidente").value(1L))
                .andExpect(jsonPath("$.idReporte").value(1L))
                .andExpect(jsonPath("$.tipoIncidente").value("CAIDA"))
                .andExpect(jsonPath("$.severidad").value("ALTA"))
                .andExpect(jsonPath("$.alertaGenerada").value(true));
    }

    @Test
    @DisplayName("POST /api/reportes-diarios/1/incidentes con campos vacíos retorna 400 Bad Request")
    void registrarIncidente_Invalido_Retorna400() throws Exception {
        String json = "{\"tipoIncidente\":\"\",\"descripcion\":\"\",\"severidad\":\"\"}";

        mockMvc.perform(post("/api/reportes-diarios/1/incidentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipoIncidente").exists())
                .andExpect(jsonPath("$.descripcion").exists())
                .andExpect(jsonPath("$.severidad").exists());
    }

    @Test
    @DisplayName("POST /api/reportes-diarios/999/incidentes para reporte no existente retorna 404")
    void registrarIncidente_ReporteNoExiste_Retorna404() throws Exception {
        String json = "{\"tipoIncidente\":\"CAIDA\",\"descripcion\":\"Detalle\",\"severidad\":\"LEVE\"}";

        when(incidenteService.registrarIncidente(eq(999L), any(IncidenteRequestDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "El reporte diario con ID 999 no existe"));

        mockMvc.perform(post("/api/reportes-diarios/999/incidentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("El reporte diario con ID 999 no existe"));
    }

    @Test
    @DisplayName("GET /api/reportes-diarios/1/incidentes retorna lista con 200 OK")
    void obtenerIncidentesPorReporte_Retorna200() throws Exception {
        IncidenteResponseDTO dto = new IncidenteResponseDTO(
                1L, 1L, "DESORIENTACION", "Confusión", "LEVE", LocalDateTime.now(), false, true
        );

        when(incidenteService.obtenerIncidentesPorReporte(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/reportes-diarios/1/incidentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idIncidente").value(1L))
                .andExpect(jsonPath("$[0].tipoIncidente").value("DESORIENTACION"))
                .andExpect(jsonPath("$[0].severidad").value("LEVE"));
    }

    @Test
    @DisplayName("GET /api/reportes-diarios/incidentes/1 retorna detalle con 200 OK")
    void obtenerIncidentePorId_Retorna200() throws Exception {
        IncidenteResponseDTO dto = new IncidenteResponseDTO(
                1L, 1L, "CAIDA", "Caída", "ALTA", LocalDateTime.now(), true, true
        );

        when(incidenteService.obtenerIncidentePorId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/reportes-diarios/incidentes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idIncidente").value(1L))
                .andExpect(jsonPath("$.tipoIncidente").value("CAIDA"))
                .andExpect(jsonPath("$.severidad").value("ALTA"))
                .andExpect(jsonPath("$.alertaGenerada").value(true));
    }
}
