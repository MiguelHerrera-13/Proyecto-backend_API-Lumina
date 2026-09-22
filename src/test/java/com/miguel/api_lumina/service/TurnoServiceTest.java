package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.TurnoRequestDTO;
import com.miguel.api_lumina.dto.TurnoResponseDTO;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.entity.TurnoEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.PacienteRepository;
import com.miguel.api_lumina.repository.TurnoRepository;
import com.miguel.api_lumina.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TurnoServiceTest {

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TurnoService turnoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void crearTurno_Valido_DeberiaCrearTurno() {
        // Arrange
        TurnoRequestDTO request = new TurnoRequestDTO();
        request.setIdPaciente(1L);
        request.setIdCuidador(2L);
        request.setFechaHoraInicio(LocalDateTime.of(2026, 1, 1, 10, 0));
        request.setFechaHoraFin(LocalDateTime.of(2026, 1, 1, 11, 0));
        request.setEstado("PENDIENTE");

        PacienteEntity paciente = new PacienteEntity();
        paciente.setIdPaciente(1L);
        paciente.setNombreCompleto("Paciente Test");

        UsuarioEntity cuidador = new UsuarioEntity();
        cuidador.setIdUsuario(2L);
        cuidador.setNombre("Cuidador Test");

        TurnoEntity turnoGuardado = new TurnoEntity();
        turnoGuardado.setIdTurno(1L);
        turnoGuardado.setPaciente(paciente);
        turnoGuardado.setCuidador(cuidador);
        turnoGuardado.setFechaHoraInicio(request.getFechaHoraInicio());
        turnoGuardado.setFechaHoraFin(request.getFechaHoraFin());
        turnoGuardado.setEstado("PENDIENTE");

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(cuidador));
        when(turnoRepository.save(any(TurnoEntity.class))).thenReturn(turnoGuardado);

        // Act
        TurnoResponseDTO response = turnoService.crearTurno(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getIdTurno());
        assertEquals("Paciente Test", response.getNombrePaciente());
        assertEquals("Cuidador Test", response.getNombreCuidador());
        verify(turnoRepository, times(1)).save(any(TurnoEntity.class));
    }

    @Test
    void crearTurno_PacienteInexistente_DeberiaLanzarExcepcion() {
        // Arrange
        TurnoRequestDTO request = new TurnoRequestDTO();
        request.setIdPaciente(99L);
        request.setIdCuidador(2L);
        request.setFechaHoraInicio(LocalDateTime.of(2026, 1, 1, 10, 0));
        request.setFechaHoraFin(LocalDateTime.of(2026, 1, 1, 11, 0));

        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            turnoService.crearTurno(request);
        });

        assertEquals("El paciente no existe", exception.getMessage());
        verify(turnoRepository, never()).save(any(TurnoEntity.class));
    }
    
    @Test
    void crearTurno_CuidadorInexistente_DeberiaLanzarExcepcion() {
        // Arrange
        TurnoRequestDTO request = new TurnoRequestDTO();
        request.setIdPaciente(1L);
        request.setIdCuidador(99L);
        request.setFechaHoraInicio(LocalDateTime.of(2026, 1, 1, 10, 0));
        request.setFechaHoraFin(LocalDateTime.of(2026, 1, 1, 11, 0));

        PacienteEntity paciente = new PacienteEntity();
        paciente.setIdPaciente(1L);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            turnoService.crearTurno(request);
        });

        assertEquals("El cuidador no existe", exception.getMessage());
        verify(turnoRepository, never()).save(any(TurnoEntity.class));
    }

    @Test
    void crearTurno_HorarioInvalido_DeberiaLanzarExcepcion() {
        // Arrange
        TurnoRequestDTO request = new TurnoRequestDTO();
        request.setIdPaciente(1L);
        request.setIdCuidador(2L);
        // Inicio posterior a fin
        request.setFechaHoraInicio(LocalDateTime.of(2026, 1, 1, 11, 0));
        request.setFechaHoraFin(LocalDateTime.of(2026, 1, 1, 10, 0));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            turnoService.crearTurno(request);
        });

        assertEquals("La fecha de inicio debe ser anterior a la de fin", exception.getMessage());
        verify(turnoRepository, never()).save(any(TurnoEntity.class));
    }
}
