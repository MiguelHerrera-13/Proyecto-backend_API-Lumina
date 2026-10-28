package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.TurnoConflictoDTO;
import com.miguel.api_lumina.dto.TurnoRecurrenteRequestDTO;
import com.miguel.api_lumina.dto.TurnoRecurrenteResponseDTO;
import com.miguel.api_lumina.dto.TurnoResponseDTO;
import com.miguel.api_lumina.exception.GlobalExceptionHandler;
import com.miguel.api_lumina.service.TurnoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TurnoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TurnoService turnoService;

    @InjectMocks
    private TurnoController turnoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(turnoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/turnos/recurrentes con datos válidos retorna 201 Created y reporte de recurrencia")
    void generarTurnosRecurrentes_Valido_Retorna201() throws Exception {
        String json = "{\"idPaciente\":1,\"idCuidador\":2,\"idMedico\":3," +
                "\"fechaHoraInicio\":\"2026-11-01T08:00:00\",\"fechaHoraFin\":\"2026-11-01T12:00:00\"," +
                "\"patronRecurrencia\":\"DIARIO\",\"fechaLimite\":\"2026-11-03\",\"estado\":\"ASIGNADO\"}";

        TurnoResponseDTO t1 = new TurnoResponseDTO();
        t1.setIdTurno(1L);
        t1.setNombrePaciente("Paciente");
        t1.setIdRecurrencia("rec-uuid");

        TurnoRecurrenteResponseDTO response = new TurnoRecurrenteResponseDTO(
                "rec-uuid",
                "DIARIO",
                2,
                1,
                List.of(t1),
                List.of(new TurnoConflictoDTO(LocalDateTime.now(), LocalDateTime.now().plusHours(4), "Solapamiento"))
        );

        when(turnoService.generarTurnosRecurrentes(any(TurnoRecurrenteRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/turnos/recurrentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idRecurrencia").value("rec-uuid"))
                .andExpect(jsonPath("$.patronRecurrencia").value("DIARIO"))
                .andExpect(jsonPath("$.totalGenerados").value(2))
                .andExpect(jsonPath("$.totalOmitidosPorConflicto").value(1));
    }

    @Test
    @DisplayName("POST /api/turnos/recurrentes con campos faltantes retorna 400 Bad Request")
    void generarTurnosRecurrentes_CamposFaltantes_Retorna400() throws Exception {
        String json = "{\"patronRecurrencia\":\"\"}";

        mockMvc.perform(post("/api/turnos/recurrentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/turnos/recurrencia/{idRecurrencia} retorna lista de turnos asociados con 200 OK")
    void obtenerTurnosPorRecurrencia_Retorna200() throws Exception {
        TurnoResponseDTO dto = new TurnoResponseDTO();
        dto.setIdTurno(10L);
        dto.setIdRecurrencia("rec-uuid");

        when(turnoService.obtenerTurnosPorRecurrencia("rec-uuid")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/turnos/recurrencia/rec-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idTurno").value(10L))
                .andExpect(jsonPath("$[0].idRecurrencia").value("rec-uuid"));
    }
}
