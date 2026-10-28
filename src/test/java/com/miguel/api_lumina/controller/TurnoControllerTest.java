package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.AgendaDisponibilidadResponseDTO;
import com.miguel.api_lumina.dto.CancelarTurnoDTO;
import com.miguel.api_lumina.dto.IntervaloAgendaDTO;
import com.miguel.api_lumina.dto.ReprogramarTurnoDTO;
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
import java.util.Collections;
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
    @DisplayName("GET /api/turnos/agenda con fechas válidas retorna 200 OK y agenda calculada")
    public void obtenerAgenda_Valido_Retorna200() throws Exception {
        LocalDateTime desde = LocalDateTime.of(2026, 5, 10, 8, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 5, 10, 18, 0);

        AgendaDisponibilidadResponseDTO agenda = new AgendaDisponibilidadResponseDTO();
        agenda.setProfesionalId(2L);
        agenda.setNombreProfesional("Dra. Perez");
        agenda.setRolProfesional("MEDICO");
        agenda.setDesde(desde);
        agenda.setHasta(hasta);
        agenda.setTurnosOcupados(Collections.emptyList());
        agenda.setHuecosLibres(List.of(new IntervaloAgendaDTO(desde, hasta, "LIBRE", null, "Disponibilidad libre")));

        when(turnoService.obtenerAgendaDisponibilidad(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(agenda);

        mockMvc.perform(get("/api/turnos/agenda")
                        .param("profesionalId", "2")
                        .param("desde", "2026-05-10T08:00:00")
                        .param("hasta", "2026-05-10T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profesionalId").value(2))
                .andExpect(jsonPath("$.nombreProfesional").value("Dra. Perez"))
                .andExpect(jsonPath("$.huecosLibres[0].tipo").value("LIBRE"));
    }

    @Test
    @DisplayName("GET /api/turnos/agenda con desde posterior a hasta retorna 400 Bad Request")
    public void obtenerAgenda_RangoInvalido_Retorna400() throws Exception {
        when(turnoService.obtenerAgendaDisponibilidad(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new IllegalArgumentException("La fecha 'desde' debe ser estrictamente anterior a la fecha 'hasta'"));

        mockMvc.perform(get("/api/turnos/agenda")
                        .param("profesionalId", "2")
                        .param("desde", "2026-05-10T18:00:00")
                        .param("hasta", "2026-05-10T08:00:00"))
                .andExpect(status().isBadRequest());
    }
}
