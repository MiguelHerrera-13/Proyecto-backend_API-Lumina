package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.IncidenteRequestDTO;
import com.miguel.api_lumina.dto.IncidenteResponseDTO;
import com.miguel.api_lumina.entity.IncidenteReporteEntity;
import com.miguel.api_lumina.entity.ReporteDiarioEntity;
import com.miguel.api_lumina.entity.TurnoEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.IncidenteReporteRepository;
import com.miguel.api_lumina.repository.ReporteDiarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidenteServiceTest {

    @Mock
    private IncidenteReporteRepository incidenteReporteRepository;

    @Mock
    private ReporteDiarioRepository reporteDiarioRepository;

    @Mock
    private AlertaService alertaService;

    @InjectMocks
    private IncidenteService incidenteService;

    private ReporteDiarioEntity reporte;
    private TurnoEntity turno;
    private UsuarioEntity medico;

    @BeforeEach
    void setUp() {
        medico = new UsuarioEntity();
        medico.setIdUsuario(10L);
        medico.setNombre("Dr. Gregory House");

        turno = new TurnoEntity();
        turno.setIdTurno(1L);
        turno.setMedico(medico);

        reporte = new ReporteDiarioEntity();
        reporte.setIdReporte(100L);
        reporte.setTurno(turno);
    }

    @Test
    @DisplayName("Registrar incidente con severidad LEVE no genera alerta y retorna 201 DTO")
    void registrarIncidente_SeveridadLeve_SinAlerta() {
        IncidenteRequestDTO request = new IncidenteRequestDTO(
                "DESORIENTACION",
                "Paciente confundido momentáneamente al despertar.",
                "LEVE",
                LocalDateTime.now()
        );

        when(reporteDiarioRepository.findById(100L)).thenReturn(Optional.of(reporte));
        when(incidenteReporteRepository.save(any(IncidenteReporteEntity.class))).thenAnswer(invocation -> {
            IncidenteReporteEntity entity = invocation.getArgument(0);
            entity.setIdIncidente(1L);
            return entity;
        });

        IncidenteResponseDTO response = incidenteService.registrarIncidente(100L, request);

        assertNotNull(response);
        assertEquals(1L, response.getIdIncidente());
        assertEquals(100L, response.getIdReporte());
        assertEquals("DESORIENTACION", response.getTipoIncidente());
        assertEquals("LEVE", response.getSeveridad());
        assertFalse(response.getAlertaGenerada());
        assertTrue(response.getActivo());

        verify(alertaService, never()).crearAlerta(any(), any(), any());
    }

    @Test
    @DisplayName("Registrar incidente con severidad ALTA dispara alerta clínica al médico del turno")
    void registrarIncidente_SeveridadAlta_DisparaAlerta() {
        IncidenteRequestDTO request = new IncidenteRequestDTO(
                "CAIDA",
                "Paciente sufrió caída al intentar levantarse de la cama.",
                "ALTA",
                LocalDateTime.now()
        );

        when(reporteDiarioRepository.findById(100L)).thenReturn(Optional.of(reporte));
        when(incidenteReporteRepository.save(any(IncidenteReporteEntity.class))).thenAnswer(invocation -> {
            IncidenteReporteEntity entity = invocation.getArgument(0);
            entity.setIdIncidente(2L);
            return entity;
        });

        IncidenteResponseDTO response = incidenteService.registrarIncidente(100L, request);

        assertNotNull(response);
        assertEquals(2L, response.getIdIncidente());
        assertEquals("CAIDA", response.getTipoIncidente());
        assertEquals("ALTA", response.getSeveridad());
        assertTrue(response.getAlertaGenerada());

        verify(alertaService, times(1)).crearAlerta(
                eq(reporte),
                eq(medico),
                contains("Incidente severo detectado: CAIDA")
        );
    }

    @Test
    @DisplayName("Registrar incidente con reporte inexistente lanza 404 NOT_FOUND")
    void registrarIncidente_ReporteNoExiste_Lanza404() {
        IncidenteRequestDTO request = new IncidenteRequestDTO(
                "CAIDA", "Detalle", "ALTA", LocalDateTime.now()
        );

        when(reporteDiarioRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> incidenteService.registrarIncidente(999L, request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("El reporte diario con ID 999 no existe"));
    }

    @Test
    @DisplayName("Registrar incidente con severidad inválida lanza 400 BAD_REQUEST")
    void registrarIncidente_SeveridadInvalida_Lanza400() {
        IncidenteRequestDTO request = new IncidenteRequestDTO(
                "CAIDA", "Detalle", "EXTREMA", LocalDateTime.now()
        );

        when(reporteDiarioRepository.findById(100L)).thenReturn(Optional.of(reporte));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> incidenteService.registrarIncidente(100L, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Severidad inválida"));
    }

    @Test
    @DisplayName("Registrar incidente severo sin médico en turno lanza 400 BAD_REQUEST")
    void registrarIncidente_SeveridadAlta_SinMedico_Lanza400() {
        turno.setMedico(null);
        IncidenteRequestDTO request = new IncidenteRequestDTO(
                "CAIDA", "Detalle", "ALTA", LocalDateTime.now()
        );

        when(reporteDiarioRepository.findById(100L)).thenReturn(Optional.of(reporte));
        when(incidenteReporteRepository.save(any(IncidenteReporteEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> incidenteService.registrarIncidente(100L, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("no tiene un médico asignado"));
    }

    @Test
    @DisplayName("Obtener incidentes por reporte exitoso")
    void obtenerIncidentesPorReporte_Exitoso() {
        IncidenteReporteEntity entity = new IncidenteReporteEntity(
                1L, reporte, LocalDateTime.now(), "DESCOMPENSACION", "Baja de presión", "ALTA", true
        );

        when(reporteDiarioRepository.existsById(100L)).thenReturn(true);
        when(incidenteReporteRepository.findByReporte_IdReporte(100L)).thenReturn(List.of(entity));

        List<IncidenteResponseDTO> lista = incidenteService.obtenerIncidentesPorReporte(100L);

        assertEquals(1, lista.size());
        assertEquals("DESCOMPENSACION", lista.get(0).getTipoIncidente());
        assertTrue(lista.get(0).getAlertaGenerada());
    }

    @Test
    @DisplayName("Obtener incidentes por reporte inexistente lanza 404")
    void obtenerIncidentesPorReporte_ReporteInexistente_Lanza404() {
        when(reporteDiarioRepository.existsById(999L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> incidenteService.obtenerIncidentesPorReporte(999L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}

