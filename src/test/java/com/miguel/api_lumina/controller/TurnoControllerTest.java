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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        mockMvc = MockMvcBuilders.standaloneSetup(turnoController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
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

    @Test
    @DisplayName("GET /api/turnos con filtros combinados retorna 200 OK y página con resultados")
    void testBuscarTurnos_ConFiltros_Retorna200YPagina() throws Exception {
        TurnoResponseDTO dto1 = new TurnoResponseDTO();
        dto1.setIdTurno(10L);
        dto1.setNombrePaciente("Carlos Gomez");
        dto1.setEstado("CONFIRMADO");

        TurnoResponseDTO dto2 = new TurnoResponseDTO();
        dto2.setIdTurno(11L);
        dto2.setNombrePaciente("Maria Lopez");
        dto2.setEstado("CONFIRMADO");

        Page<TurnoResponseDTO> page = new PageImpl<>(List.of(dto1, dto2), PageRequest.of(0, 10), 2);

        when(turnoService.buscarTurnos(
                eq(1L), eq(2L), any(), any(), eq("CONFIRMADO"), any(), any(), any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/turnos")
                        .param("pacienteId", "1")
                        .param("profesionalId", "2")
                        .param("estado", "CONFIRMADO")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].idTurno").value(10))
                .andExpect(jsonPath("$.content[0].nombrePaciente").value("Carlos Gomez"))
                .andExpect(jsonPath("$.content[1].idTurno").value(11))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/turnos sin coincidencias retorna 200 OK con lista vacía")
    void testBuscarTurnos_SinCoincidencias_Retorna200YListaVacia() throws Exception {
        Page<TurnoResponseDTO> pageVacia = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(turnoService.buscarTurnos(
                any(), any(), any(), any(), eq("INEXISTENTE"), any(), any(), any(Pageable.class)
        )).thenReturn(pageVacia);

        mockMvc.perform(get("/api/turnos")
                        .param("estado", "INEXISTENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /api/turnos con paginación y ordenamiento configurable retorna 200 OK")
    void testBuscarTurnos_PaginacionYOrden_Retorna200() throws Exception {
        Page<TurnoResponseDTO> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 5), 15);

        when(turnoService.buscarTurnos(
                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/turnos")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "fechaHoraInicio,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(15));
    }
}
