package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.ReporteDiarioRequestDTO;
import com.miguel.api_lumina.entity.ReporteDiarioEntity;
import com.miguel.api_lumina.entity.TurnoEntity;
import com.miguel.api_lumina.repository.ReporteDiarioRepository;
import com.miguel.api_lumina.repository.TurnoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteDiarioServiceTest {

    @Mock
    private ReporteDiarioRepository reporteDiarioRepository;

    @Mock
    private TurnoRepository turnoRepository;

    @InjectMocks
    private ReporteDiarioService reporteDiarioService;

    private TurnoEntity turno;
    private ReporteDiarioRequestDTO requestValido;

    @BeforeEach
    void setUp() {
        turno = new TurnoEntity();
        turno.setIdTurno(1L);

        requestValido = new ReporteDiarioRequestDTO();
        requestValido.setIdTurno(1L);
        requestValido.setTemperatura(36.6);
        requestValido.setPresionArterial("120/80");
        requestValido.setObservacion("Paciente estable y descansando.");
    }

    @Test
    @DisplayName("Debe registrar un reporte diario exitosamente (Caso Válido)")
    void registrarReporte_exitoso() {
        // Given
        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));
        when(reporteDiarioRepository.save(any(ReporteDiarioEntity.class))).thenAnswer(invocation -> {
            ReporteDiarioEntity r = invocation.getArgument(0);
            r.setIdReporte(10L);
            return r;
        });

        // When
        ReporteDiarioEntity resultado = reporteDiarioService.registrarReporte(requestValido);

        // Then
        assertNotNull(resultado);
        assertEquals(10L, resultado.getIdReporte());
        assertEquals(36.6, resultado.getTemperatura());
        assertEquals("120/80", resultado.getPresionArterial());
        assertEquals("Paciente estable y descansando.", resultado.getObservacion());
        assertEquals(1L, resultado.getTurno().getIdTurno());
        verify(reporteDiarioRepository, times(1)).save(any(ReporteDiarioEntity.class));
    }

    @Test
    @DisplayName("Debe fallar al registrar si el turno no existe (Caso Inválido)")
    void registrarReporte_turnoInexistente_lanzaExcepcion() {
        // Given
        when(turnoRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            reporteDiarioService.registrarReporte(requestValido);
        });

        assertTrue(ex.getMessage().contains("El turno con ID 1 no existe"));
        verify(reporteDiarioRepository, never()).save(any(ReporteDiarioEntity.class));
    }

    @Test
    @DisplayName("Debe obtener lista de reportes por turno existente (Caso Válido)")
    void obtenerReportesPorTurno_exitoso() {
        // Given
        ReporteDiarioEntity reporte = new ReporteDiarioEntity();
        reporte.setIdReporte(10L);
        reporte.setTurno(turno);

        when(turnoRepository.existsById(1L)).thenReturn(true);
        when(reporteDiarioRepository.findByTurno_IdTurno(1L)).thenReturn(List.of(reporte));

        // When
        List<ReporteDiarioEntity> resultado = reporteDiarioService.obtenerReportesPorTurno(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getIdReporte());
    }

    @Test
    @DisplayName("Debe fallar al consultar si el turno no existe (Caso Inválido)")
    void obtenerReportesPorTurno_turnoInexistente_lanzaExcepcion() {
        // Given
        when(turnoRepository.existsById(99L)).thenReturn(false);

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            reporteDiarioService.obtenerReportesPorTurno(99L);
        });

        assertTrue(ex.getMessage().contains("El turno con ID 99 no existe"));
        verify(reporteDiarioRepository, never()).findByTurno_IdTurno(anyLong());
    }
}
