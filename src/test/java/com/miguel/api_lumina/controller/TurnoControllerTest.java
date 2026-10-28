package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.CancelarTurnoDTO;
import com.miguel.api_lumina.dto.ReprogramarTurnoDTO;
import com.miguel.api_lumina.dto.TurnoResponseDTO;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TurnoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TurnoService turnoService;

    @InjectMocks
    private TurnoController turnoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(turnoController).build();
    }

    @Test
    @DisplayName("POST /api/turnos/{id}/cancelar con motivo válido retorna 200 y estado CANCELADO")
    void testCancelarTurno_Exitoso() throws Exception {
        String jsonRequest = "{\"motivo\":\"Paciente presenta cuadro febril y debe guardar reposo\"}";

        TurnoResponseDTO response = new TurnoResponseDTO();
        response.setIdTurno(1L);
        response.setEstado("CANCELADO");
        response.setMotivoCancelacion("Paciente presenta cuadro febril y debe guardar reposo");

        when(turnoService.cancelarTurno(eq(1L), any(CancelarTurnoDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/turnos/1/cancelar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTurno").value(1))
                .andExpect(jsonPath("$.estado").value("CANCELADO"))
                .andExpect(jsonPath("$.motivoCancelacion").value("Paciente presenta cuadro febril y debe guardar reposo"));
    }

    @Test
    @DisplayName("POST /api/turnos/{id}/cancelar con turno finalizado retorna 409 Conflict")
    void testCancelarTurno_ConflictoEstado() throws Exception {
        String jsonRequest = "{\"motivo\":\"Paciente presenta cuadro febril y debe guardar reposo\"}";

        when(turnoService.cancelarTurno(eq(1L), any(CancelarTurnoDTO.class)))
                .thenThrow(new IllegalStateException("No se puede cancelar un turno que ya ha sido finalizado"));

        mockMvc.perform(post("/api/turnos/1/cancelar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(content().string("No se puede cancelar un turno que ya ha sido finalizado"));
    }

    @Test
    @DisplayName("POST /api/turnos/{id}/reprogramar con datos válidos retorna 200 y estado REPROGRAMADO")
    void testReprogramarTurno_Exitoso() throws Exception {
        String jsonRequest = "{\"nuevaFechaHoraInicio\":\"2026-03-10T10:00:00\",\"nuevaFechaHoraFin\":\"2026-03-10T11:00:00\",\"motivo\":\"Solicitud justificada por viaje laboral familiar\"}";

        TurnoResponseDTO response = new TurnoResponseDTO();
        response.setIdTurno(1L);
        response.setEstado("REPROGRAMADO");
        response.setFechaHoraInicio(LocalDateTime.of(2026, 3, 10, 10, 0));
        response.setFechaHoraFin(LocalDateTime.of(2026, 3, 10, 11, 0));
        response.setMotivoReprogramacion("Solicitud justificada por viaje laboral familiar");

        when(turnoService.reprogramarTurno(eq(1L), any(ReprogramarTurnoDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/turnos/1/reprogramar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTurno").value(1))
                .andExpect(jsonPath("$.estado").value("REPROGRAMADO"))
                .andExpect(jsonPath("$.motivoReprogramacion").value("Solicitud justificada por viaje laboral familiar"));
    }

    @Test
    @DisplayName("POST /api/turnos/{id}/reprogramar con conflicto de horario retorna 409 Conflict")
    void testReprogramarTurno_ConflictoDisponibilidad() throws Exception {
        String jsonRequest = "{\"nuevaFechaHoraInicio\":\"2026-03-10T10:00:00\",\"nuevaFechaHoraFin\":\"2026-03-10T11:00:00\",\"motivo\":\"Solicitud justificada por viaje laboral familiar\"}";

        when(turnoService.reprogramarTurno(eq(1L), any(ReprogramarTurnoDTO.class)))
                .thenThrow(new IllegalStateException("El médico ya tiene un turno asignado en el horario seleccionado"));

        mockMvc.perform(post("/api/turnos/1/reprogramar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(content().string("El médico ya tiene un turno asignado en el horario seleccionado"));
    }
}
