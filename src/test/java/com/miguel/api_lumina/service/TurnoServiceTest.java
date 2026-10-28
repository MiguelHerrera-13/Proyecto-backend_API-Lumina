package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AgendaDisponibilidadResponseDTO;
import com.miguel.api_lumina.dto.IntervaloAgendaDTO;
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
import java.util.ArrayList;
import java.util.List;
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
        request.setIdMedico(3L);
        request.setFechaHoraInicio(LocalDateTime.of(2026, 1, 1, 10, 0));
        request.setFechaHoraFin(LocalDateTime.of(2026, 1, 1, 11, 0));
        request.setEstado("PENDIENTE");

        PacienteEntity paciente = new PacienteEntity();
        paciente.setIdPaciente(1L);
        paciente.setNombreCompleto("Paciente Test");

        UsuarioEntity cuidador = new UsuarioEntity();
        cuidador.setIdUsuario(2L);
        cuidador.setNombre("Cuidador Test");

        UsuarioEntity medico = new UsuarioEntity();
        medico.setIdUsuario(3L);
        medico.setNombre("Medico Test");
        medico.setRol("MEDICO");

        TurnoEntity turnoGuardado = new TurnoEntity();
        turnoGuardado.setIdTurno(1L);
        turnoGuardado.setPaciente(paciente);
        turnoGuardado.setCuidador(cuidador);
        turnoGuardado.setMedico(medico);
        turnoGuardado.setFechaHoraInicio(request.getFechaHoraInicio());
        turnoGuardado.setFechaHoraFin(request.getFechaHoraFin());
        turnoGuardado.setEstado("PENDIENTE");

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(cuidador));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(medico));
        when(turnoRepository.save(any(TurnoEntity.class))).thenReturn(turnoGuardado);

        // Act
        TurnoResponseDTO response = turnoService.crearTurno(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getIdTurno());
        assertEquals("Paciente Test", response.getNombrePaciente());
        assertEquals("Cuidador Test", response.getNombreCuidador());
        assertEquals(3L, response.getIdMedico());
        assertEquals("Medico Test", response.getNombreMedico());
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
        request.setFechaHoraInicio(LocalDateTime.of(2026, 1, 1, 11, 0));
        request.setFechaHoraFin(LocalDateTime.of(2026, 1, 1, 10, 0));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            turnoService.crearTurno(request);
        });

        assertEquals("La fecha de inicio debe ser anterior a la de fin", exception.getMessage());
        verify(turnoRepository, never()).save(any(TurnoEntity.class));
    }

    @Test
    void obtenerAgendaDisponibilidad_FechasValidas_CalculaHuecosLibres() {
        // Arrange
        Long profesionalId = 2L;
        LocalDateTime desde = LocalDateTime.of(2026, 5, 10, 8, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 5, 10, 18, 0);

        UsuarioEntity profesional = new UsuarioEntity();
        profesional.setIdUsuario(2L);
        profesional.setNombre("Dra. Perez");
        profesional.setRol("MEDICO");

        PacienteEntity paciente = new PacienteEntity();
        paciente.setIdPaciente(1L);
        paciente.setNombreCompleto("Paciente Uno");

        TurnoEntity turno1 = new TurnoEntity();
        turno1.setIdTurno(101L);
        turno1.setPaciente(paciente);
        turno1.setCuidador(profesional);
        turno1.setFechaHoraInicio(LocalDateTime.of(2026, 5, 10, 10, 0));
        turno1.setFechaHoraFin(LocalDateTime.of(2026, 5, 10, 12, 0));
        turno1.setEstado("CONFIRMADO");

        TurnoEntity turno2 = new TurnoEntity();
        turno2.setIdTurno(102L);
        turno2.setPaciente(paciente);
        turno2.setCuidador(profesional);
        turno2.setFechaHoraInicio(LocalDateTime.of(2026, 5, 10, 14, 0));
        turno2.setFechaHoraFin(LocalDateTime.of(2026, 5, 10, 15, 30));
        turno2.setEstado("CONFIRMADO");

        List<TurnoEntity> turnosOcupados = List.of(turno1, turno2);

        when(usuarioRepository.findById(profesionalId)).thenReturn(Optional.of(profesional));
        when(turnoRepository.findAgendaProfesional(profesionalId, desde, hasta)).thenReturn(turnosOcupados);

        // Act
        AgendaDisponibilidadResponseDTO result = turnoService.obtenerAgendaDisponibilidad(profesionalId, desde, hasta);

        // Assert
        assertNotNull(result);
        assertEquals(profesionalId, result.getProfesionalId());
        assertEquals("Dra. Perez", result.getNombreProfesional());
        assertEquals(2, result.getTurnosOcupados().size());

        List<IntervaloAgendaDTO> huecos = result.getHuecosLibres();
        assertNotNull(huecos);
        assertEquals(3, huecos.size());

        // Hueco 1: 08:00 a 10:00
        assertEquals(desde, huecos.get(0).getInicio());
        assertEquals(LocalDateTime.of(2026, 5, 10, 10, 0), huecos.get(0).getFin());

        // Hueco 2: 12:00 a 14:00
        assertEquals(LocalDateTime.of(2026, 5, 10, 12, 0), huecos.get(1).getInicio());
        assertEquals(LocalDateTime.of(2026, 5, 10, 14, 0), huecos.get(1).getFin());

        // Hueco 3: 15:30 a 18:00
        assertEquals(LocalDateTime.of(2026, 5, 10, 15, 30), huecos.get(2).getInicio());
        assertEquals(hasta, huecos.get(2).getFin());
    }

    @Test
    void obtenerAgendaDisponibilidad_SinTurnos_TodoElRangoEsLibre() {
        // Arrange
        Long profesionalId = 2L;
        LocalDateTime desde = LocalDateTime.of(2026, 5, 10, 8, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 5, 10, 12, 0);

        UsuarioEntity profesional = new UsuarioEntity();
        profesional.setIdUsuario(2L);
        profesional.setNombre("Dr. Lopez");
        profesional.setRol("CUIDADOR");

        when(usuarioRepository.findById(profesionalId)).thenReturn(Optional.of(profesional));
        when(turnoRepository.findAgendaProfesional(profesionalId, desde, hasta)).thenReturn(new ArrayList<>());

        // Act
        AgendaDisponibilidadResponseDTO result = turnoService.obtenerAgendaDisponibilidad(profesionalId, desde, hasta);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTurnosOcupados().size());
        assertEquals(1, result.getHuecosLibres().size());
        assertEquals(desde, result.getHuecosLibres().get(0).getInicio());
        assertEquals(hasta, result.getHuecosLibres().get(0).getFin());
    }

    @Test
    void obtenerAgendaDisponibilidad_DesdePosteriorAHasta_LanzaExcepcion() {
        LocalDateTime desde = LocalDateTime.of(2026, 5, 10, 18, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 5, 10, 8, 0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                turnoService.obtenerAgendaDisponibilidad(1L, desde, hasta)
        );
        assertTrue(ex.getMessage().contains("estrictamente anterior"));
    }

    @Test
    void obtenerAgendaDisponibilidad_FechasNulas_LanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                turnoService.obtenerAgendaDisponibilidad(1L, null, LocalDateTime.now())
        );
        assertTrue(ex.getMessage().contains("son obligatorios"));
    }

    @Test
    void obtenerAgendaDisponibilidad_ProfesionalInexistente_LanzaExcepcion() {
        LocalDateTime desde = LocalDateTime.of(2026, 5, 10, 8, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 5, 10, 12, 0);

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                turnoService.obtenerAgendaDisponibilidad(999L, desde, hasta)
        );
        assertTrue(ex.getMessage().contains("no existe"));
    }
}
