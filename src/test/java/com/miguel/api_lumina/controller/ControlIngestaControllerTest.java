package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.ControlIngestaRequestDTO;
import com.miguel.api_lumina.dto.ControlIngestaResponseDTO;
import com.miguel.api_lumina.dto.ResumenIngestaResponseDTO;
import com.miguel.api_lumina.exception.GlobalExceptionHandler;
import com.miguel.api_lumina.service.ControlIngestaService;
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
class ControlIngestaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ControlIngestaService controlIngestaService;

    @InjectMocks
    private ControlIngestaController controlIngestaController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controlIngestaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/reportes-diarios/1/ingestas con datos válidos retorna 201 Created")
    void registrarIngesta_Valido_Retorna201() throws Exception {
        String json = "{\"tipoComida\":\"ALMUERZO\",\"descripcionComida\":\"Pollo con ensalada\",\"porcentajeConsumido\":90,\"aguaMl\":300}";

        ControlIngestaResponseDTO response = new ControlIngestaResponseDTO(
                1L,
                1L,
                "ALMUERZO",
                "Pollo con ensalada",
                90,
                300,
                LocalDateTime.now(),
                null,
                true
        );

        when(controlIngestaService.registrarIngesta(eq(1L), any(ControlIngestaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/reportes-diarios/1/ingestas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idControlIngesta").value(1L))
                .andExpect(jsonPath("$.idReporte").value(1L))
                .andExpect(jsonPath("$.tipoComida").value("ALMUERZO"))
                .andExpect(jsonPath("$.porcentajeConsumido").value(90))
                .andExpect(jsonPath("$.aguaMl").value(300));
    }

    @Test
    @DisplayName("POST /api/reportes-diarios/1/ingestas con porcentaje > 100 retorna 400 Bad Request")
    void registrarIngesta_PorcentajeInvalido_Retorna400() throws Exception {
        String json = "{\"tipoComida\":\"ALMUERZO\",\"porcentajeConsumido\":150,\"aguaMl\":200}";

        mockMvc.perform(post("/api/reportes-diarios/1/ingestas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.porcentajeConsumido").exists());
    }

    @Test
    @DisplayName("POST /api/reportes-diarios/1/ingestas con agua negativa retorna 400 Bad Request")
    void registrarIngesta_AguaNegativa_Retorna400() throws Exception {
        String json = "{\"tipoComida\":\"ALMUERZO\",\"porcentajeConsumido\":80,\"aguaMl\":-10}";

        mockMvc.perform(post("/api/reportes-diarios/1/ingestas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.aguaMl").exists());
    }

    @Test
    @DisplayName("GET /api/reportes-diarios/1/ingestas retorna lista con 200 OK")
    void obtenerIngestasPorReporte_Retorna200() throws Exception {
        ControlIngestaResponseDTO dto = new ControlIngestaResponseDTO(
                1L, 1L, "DESAYUNO", "Café con leche", 100, 200, LocalDateTime.now(), null, true
        );

        when(controlIngestaService.obtenerIngestasPorReporte(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/reportes-diarios/1/ingestas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idControlIngesta").value(1L))
                .andExpect(jsonPath("$[0].tipoComida").value("DESAYUNO"))
                .andExpect(jsonPath("$[0].aguaMl").value(200));
    }

    @Test
    @DisplayName("GET /api/reportes-diarios/1/ingestas/resumen retorna consolidado con 200 OK")
    void obtenerResumenIngestaPorReporte_Retorna200() throws Exception {
        ControlIngestaResponseDTO dto = new ControlIngestaResponseDTO(
                1L, 1L, "ALMUERZO", "Comida", 80, 500, LocalDateTime.now(), null, true
        );

        ResumenIngestaResponseDTO resumen = new ResumenIngestaResponseDTO(
                1L, 500, 1, 80.0, List.of(dto)
        );

        when(controlIngestaService.obtenerResumenIngestaPorReporte(1L)).thenReturn(resumen);

        mockMvc.perform(get("/api/reportes-diarios/1/ingestas/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReporte").value(1L))
                .andExpect(jsonPath("$.totalAguaMl").value(500))
                .andExpect(jsonPath("$.cantidadIngestas").value(1))
                .andExpect(jsonPath("$.porcentajeConsumoPromedio").value(80.0));
    }

    @Test
    @DisplayName("GET /api/reportes-diarios/ingestas/1 retorna detalle con 200 OK")
    void obtenerIngestaPorId_Retorna200() throws Exception {
        ControlIngestaResponseDTO dto = new ControlIngestaResponseDTO(
                1L, 1L, "CENA", "Sopa", 100, 400, LocalDateTime.now(), null, true
        );

        when(controlIngestaService.obtenerIngestaPorId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/reportes-diarios/ingestas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idControlIngesta").value(1L))
                .andExpect(jsonPath("$.tipoComida").value("CENA"))
                .andExpect(jsonPath("$.aguaMl").value(400));
    }
}
