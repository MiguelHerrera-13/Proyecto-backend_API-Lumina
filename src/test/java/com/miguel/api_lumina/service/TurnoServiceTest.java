package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.CancelarTurnoDTO;
import com.miguel.api_lumina.dto.ReprogramarTurnoDTO;
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
        TurnoRequestDTO request = new TurnoRequestDTO();
        request.setIdPaciente(99L);
        request.setIdCuidador(2L);
        request.setFechaHoraInicio(LocalDateTime.of(2026, 1, 1, 10, 0));
        request.setFechaHoraFin(LocalDateTime.of(2026, 1, 1, 11, 0));

        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            turnoService.crearTurno(request);
        });

        assertEquals("El paciente no existe", exception.getMessage());
        verify(turnoRepository, never()).save(any(TurnoEntity.class));
    }

    @Test
    void crearTurno_CuidadorInexistente_DeberiaLanzarExcepcion() {
        TurnoRequestDTO request = new TurnoRequestDTO();
        request.setIdPaciente(1L);
        request.setIdCuidador(99L);
        request.setFechaHoraInicio(LocalDateTime.of(2026, 1, 1, 10, 0));
        request.setFechaHoraFin(LocalDateTime.of(2026, 1, 1, 11, 0));

        PacienteEntity paciente = new PacienteEntity();
        paciente.setIdPaciente(1L);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            turnoService.crearTurno(request);
        });

        assertEquals("El cuidador no existe", exception.getMessage());
        verify(turnoRepository, never()).save(any(TurnoEntity.class));
    }

    @Test
    void crearTurno_HorarioInvalido_DeberiaLanzarExcepcion() {
        TurnoRequestDTO request = new TurnoRequestDTO();
        request.setIdPaciente(1L);
        request.setIdCuidador(2L);
        request.setFechaHoraInicio(LocalDateTime.of(2026, 1, 1, 11, 0));
        request.setFechaHoraFin(LocalDateTime.of(2026, 1, 1, 10, 0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            turnoService.crearTurno(request);
        });

        assertEquals("La fecha de inicio debe ser anterior a la de fin", exception.getMessage());
        verify(turnoRepository, never()).save(any(TurnoEntity.class));
    }

    @Test
    void cancelarTurno_Valido_DeberiaCancelarTurno() {
        TurnoEntity turno = crearTurnoEntityBase(1L, "PENDIENTE");
        CancelarTurnoDTO dto = new CancelarTurnoDTO("El paciente presenta cuadro febril y debe guardar reposo");

        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));
        when(turnoRepository.save(any(TurnoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TurnoResponseDTO response = turnoService.cancelarTurno(1L, dto);

        assertNotNull(response);
        assertEquals("CANCELADO", response.getEstado());
        assertEquals("El paciente presenta cuadro febril y debe guardar reposo", response.getMotivoCancelacion());
        verify(turnoRepository).save(turno);
    }

    @Test
    void cancelarTurno_MotivoMenorA10Caracteres_DeberiaLanzarExcepcion() {
        CancelarTurnoDTO dto = new CancelarTurnoDTO("Fiebre");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            turnoService.cancelarTurno(1L, dto);
        });

        assertTrue(ex.getMessage().contains("al menos 10 caracteres"));
        verify(turnoRepository, never()).save(any(TurnoEntity.class));
    }

    @Test
    void cancelarTurno_TurnoFinalizado_DeberiaLanzarExcepcion() {
        TurnoEntity turno = crearTurnoEntityBase(1L, "FINALIZADO");
        CancelarTurnoDTO dto = new CancelarTurnoDTO("El paciente presenta cuadro febril y debe guardar reposo");

        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            turnoService.cancelarTurno(1L, dto);
        });

        assertTrue(ex.getMessage().contains("ya ha sido finalizado"));
        verify(turnoRepository, never()).save(any(TurnoEntity.class));
    }

    @Test
    void cancelarTurno_TurnoYaCancelado_DeberiaLanzarExcepcion() {
        TurnoEntity turno = crearTurnoEntityBase(1L, "CANCELADO");
        CancelarTurnoDTO dto = new CancelarTurnoDTO("El paciente presenta cuadro febril y debe guardar reposo");

        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            turnoService.cancelarTurno(1L, dto);
        });

        assertTrue(ex.getMessage().contains("ya se encuentra cancelado"));
        verify(turnoRepository, never()).save(any(TurnoEntity.class));
    }

    @Test
    void reprogramarTurno_Valido_DeberiaReprogramarTurno() {
        TurnoEntity turno = crearTurnoEntityBase(1L, "PENDIENTE");
        LocalDateTime nuevoInicio = LocalDateTime.of(2026, 2, 1, 14, 0);
        LocalDateTime nuevoFin = LocalDateTime.of(2026, 2, 1, 15, 0);
        ReprogramarTurnoDTO dto = new ReprogramarTurnoDTO(nuevoInicio, nuevoFin, "Solicitud del familiar por viaje laboral");

        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));
        when(turnoRepository.existsSuperposicionMedico(eq(1L), anyLong(), eq(nuevoInicio), eq(nuevoFin))).thenReturn(false);
        when(turnoRepository.existsSuperposicionCuidador(eq(1L), anyLong(), eq(nuevoInicio), eq(nuevoFin))).thenReturn(false);
        when(turnoRepository.save(any(TurnoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TurnoResponseDTO response = turnoService.reprogramarTurno(1L, dto);

        assertNotNull(response);
        assertEquals("REPROGRAMADO", response.getEstado());
        assertEquals(nuevoInicio, response.getFechaHoraInicio());
        assertEquals(nuevoFin, response.getFechaHoraFin());
        assertEquals("Solicitud del familiar por viaje laboral", response.getMotivoReprogramacion());
        verify(turnoRepository).save(turno);
    }

    @Test
    void reprogramarTurno_HorarioSuperpuestoMedico_DeberiaLanzarExcepcion() {
        TurnoEntity turno = crearTurnoEntityBase(1L, "PENDIENTE");
        LocalDateTime nuevoInicio = LocalDateTime.of(2026, 2, 1, 14, 0);
        LocalDateTime nuevoFin = LocalDateTime.of(2026, 2, 1, 15, 0);
        ReprogramarTurnoDTO dto = new ReprogramarTurnoDTO(nuevoInicio, nuevoFin, "Solicitud del familiar por viaje laboral");

        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));
        when(turnoRepository.existsSuperposicionMedico(eq(1L), anyLong(), eq(nuevoInicio), eq(nuevoFin))).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            turnoService.reprogramarTurno(1L, dto);
        });

        assertTrue(ex.getMessage().contains("médico ya tiene un turno"));
        verify(turnoRepository, never()).save(any(TurnoEntity.class));
    }

    @Test
    void reprogramarTurno_TurnoFinalizado_DeberiaLanzarExcepcion() {
        TurnoEntity turno = crearTurnoEntityBase(1L, "FINALIZADO");
        LocalDateTime nuevoInicio = LocalDateTime.of(2026, 2, 1, 14, 0);
        LocalDateTime nuevoFin = LocalDateTime.of(2026, 2, 1, 15, 0);
        ReprogramarTurnoDTO dto = new ReprogramarTurnoDTO(nuevoInicio, nuevoFin, "Solicitud del familiar por viaje laboral");

        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            turnoService.reprogramarTurno(1L, dto);
        });

        assertTrue(ex.getMessage().contains("ya ha sido finalizado"));
        verify(turnoRepository, never()).save(any(TurnoEntity.class));
    }

    private TurnoEntity crearTurnoEntityBase(Long idTurno, String estado) {
        PacienteEntity paciente = new PacienteEntity();
        paciente.setIdPaciente(1L);
        paciente.setNombreCompleto("Paciente Test");

        UsuarioEntity cuidador = new UsuarioEntity();
        cuidador.setIdUsuario(2L);
        cuidador.setNombre("Cuidador Test");

        UsuarioEntity medico = new UsuarioEntity();
        medico.setIdUsuario(3L);
        medico.setNombre("Dr. Perez");
        medico.setRol("MEDICO");

        TurnoEntity turno = new TurnoEntity();
        turno.setIdTurno(idTurno);
        turno.setPaciente(paciente);
        turno.setCuidador(cuidador);
        turno.setMedico(medico);
        turno.setFechaHoraInicio(LocalDateTime.of(2026, 1, 15, 10, 0));
        turno.setFechaHoraFin(LocalDateTime.of(2026, 1, 15, 11, 0));
        turno.setEstado(estado);
        return turno;
    }
}
