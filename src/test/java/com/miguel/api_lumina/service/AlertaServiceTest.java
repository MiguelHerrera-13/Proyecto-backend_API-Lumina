package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AlertaResponseDTO;
import com.miguel.api_lumina.entity.AlertaEntity;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.entity.ReporteDiarioEntity;
import com.miguel.api_lumina.entity.TurnoEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.AlertaRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @InjectMocks
    private AlertaService alertaService;

    private UsuarioEntity medico;
    private UsuarioEntity otroMedico;
    private ReporteDiarioEntity reporte;
    private AlertaEntity alertaNoLeida;
    private AlertaEntity alertaLeida;

    @BeforeEach
    void setUp() {
        medico = new UsuarioEntity();
        medico.setIdUsuario(10L);
        medico.setNombre("Dr. Gregory House");

        otroMedico = new UsuarioEntity();
        otroMedico.setIdUsuario(20L);
        medico.setNombre("Dra. Lisa Cuddy");

        PacienteEntity paciente = new PacienteEntity();
        paciente.setIdPaciente(1L);
        paciente.setNombreCompleto("Juan Perez");

        TurnoEntity turno = new TurnoEntity();
        turno.setIdTurno(100L);
        turno.setPaciente(paciente);
        turno.setMedico(medico);

        reporte = new ReporteDiarioEntity();
        reporte.setIdReporte(50L);
        reporte.setTurno(turno);
        reporte.setFechaRegistro(LocalDateTime.now());

        alertaNoLeida = new AlertaEntity();
        alertaNoLeida.setIdAlerta(1L);
        alertaNoLeida.setMedico(medico);
        alertaNoLeida.setReporte(reporte);
        alertaNoLeida.setMensajeAlerta("Presion arterial critica detectada: 190/120");
        alertaNoLeida.setLeida(false);

        alertaLeida = new AlertaEntity();
        alertaLeida.setIdAlerta(2L);
        alertaLeida.setMedico(medico);
        alertaLeida.setReporte(reporte);
        alertaLeida.setMensajeAlerta("Temperatura critica detectada: 39.5 C");
        alertaLeida.setLeida(true);
    }

    @Test
    @DisplayName("Debe consultar alertas no leídas por defecto cuando el filtro es null")
    void obtenerAlertasPorMedicoYEstado_filtroNull_retornaNoLeidas() {
        when(alertaRepository.findByMedico_IdUsuarioAndLeida(10L, false))
                .thenReturn(List.of(alertaNoLeida));

        List<AlertaResponseDTO> resultado = alertaService.obtenerAlertasPorMedicoYEstado(10L, null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertFalse(resultado.get(0).getLeida());
        assertEquals("Juan Perez", resultado.get(0).getNombrePaciente());
        verify(alertaRepository, times(1)).findByMedico_IdUsuarioAndLeida(10L, false);
    }

    @Test
    @DisplayName("Debe consultar alertas leídas cuando el filtro leida es true")
    void obtenerAlertasPorMedicoYEstado_filtroTrue_retornaLeidas() {
        when(alertaRepository.findByMedico_IdUsuarioAndLeida(10L, true))
                .thenReturn(List.of(alertaLeida));

        List<AlertaResponseDTO> resultado = alertaService.obtenerAlertasPorMedicoYEstado(10L, true);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(2L, resultado.get(0).getId());
        assertTrue(resultado.get(0).getLeida());
        verify(alertaRepository, times(1)).findByMedico_IdUsuarioAndLeida(10L, true);
    }

    @Test
    @DisplayName("Debe consultar alertas no leídas cuando el filtro leida es false")
    void obtenerAlertasPorMedicoYEstado_filtroFalse_retornaNoLeidas() {
        when(alertaRepository.findByMedico_IdUsuarioAndLeida(10L, false))
                .thenReturn(List.of(alertaNoLeida));

        List<AlertaResponseDTO> resultado = alertaService.obtenerAlertasPorMedicoYEstado(10L, false);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertFalse(resultado.get(0).getLeida());
        verify(alertaRepository, times(1)).findByMedico_IdUsuarioAndLeida(10L, false);
    }

    @Test
    @DisplayName("Debe marcar como leída una alerta existente asignada al médico")
    void marcarAlertaComoLeida_valida_actualizaExitosamente() {
        when(alertaRepository.findById(1L)).thenReturn(Optional.of(alertaNoLeida));
        when(alertaRepository.save(any(AlertaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AlertaResponseDTO resultado = alertaService.marcarAlertaComoLeida(10L, 1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertTrue(resultado.getLeida());
        verify(alertaRepository, times(1)).save(alertaNoLeida);
    }

    @Test
    @DisplayName("Marcar una alerta ya leída debe ser idempotente y no generar errores ni escrituras redundantes")
    void marcarAlertaComoLeida_yaLeida_esIdempotente() {
        when(alertaRepository.findById(2L)).thenReturn(Optional.of(alertaLeida));

        AlertaResponseDTO resultado = alertaService.marcarAlertaComoLeida(10L, 2L);

        assertNotNull(resultado);
        assertEquals(2L, resultado.getId());
        assertTrue(resultado.getLeida());
        verify(alertaRepository, never()).save(any(AlertaEntity.class));
    }

    @Test
    @DisplayName("Debe fallar con NOT_FOUND cuando la alerta no existe")
    void marcarAlertaComoLeida_inexistente_lanzaNotFound() {
        when(alertaRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            alertaService.marcarAlertaComoLeida(10L, 999L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Alerta no encontrada"));
        verify(alertaRepository, never()).save(any(AlertaEntity.class));
    }

    @Test
    @DisplayName("Debe fallar con NOT_FOUND si la alerta pertenece a otro médico sin revelar datos")
    void marcarAlertaComoLeida_perteneceAOtroMedico_lanzaNotFound() {
        alertaNoLeida.setMedico(otroMedico); // Pertenece al médico 20
        when(alertaRepository.findById(1L)).thenReturn(Optional.of(alertaNoLeida));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            // El médico 10 intenta marcar una alerta que es del médico 20
            alertaService.marcarAlertaComoLeida(10L, 1L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Alerta no encontrada"));
        verify(alertaRepository, never()).save(any(AlertaEntity.class));
    }
}
