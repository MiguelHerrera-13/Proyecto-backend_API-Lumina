package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.AgendaDisponibilidadResponseDTO;
import com.miguel.api_lumina.dto.IntervaloAgendaDTO;
import com.miguel.api_lumina.exception.GlobalExceptionHandler;
import com.miguel.api_lumina.service.TurnoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TurnoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TurnoService turnoService;

    @InjectMocks
    private TurnoController turnoController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(turnoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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
