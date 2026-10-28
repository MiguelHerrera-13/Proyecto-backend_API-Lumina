package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AgendaDisponibilidadResponseDTO;
import com.miguel.api_lumina.dto.CancelarTurnoDTO;
import com.miguel.api_lumina.dto.IntervaloAgendaDTO;
import com.miguel.api_lumina.dto.ReprogramarTurnoDTO;
import com.miguel.api_lumina.dto.TurnoConflictoDTO;
import com.miguel.api_lumina.dto.TurnoRecurrenteRequestDTO;
import com.miguel.api_lumina.dto.TurnoRecurrenteResponseDTO;
import com.miguel.api_lumina.dto.TurnoRequestDTO;
import com.miguel.api_lumina.dto.TurnoResponseDTO;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.entity.TurnoEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.PacienteRepository;
import com.miguel.api_lumina.repository.TurnoRepository;
import com.miguel.api_lumina.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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

    private PacienteEntity paciente;
    private UsuarioEntity cuidador;
    private UsuarioEntity medico;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        turnoService.setMaxMesesRecurrencia(3);

        paciente = new PacienteEntity();
        paciente.setIdPaciente(1L);
        paciente.setNombreCompleto("Paciente Test");

        cuidador = new UsuarioEntity();
        cuidador.setIdUsuario(2L);
        cuidador.setNombre("Cuidador Test");
        cuidador.setRol("CUIDADOR");

        medico = new UsuarioEntity();
        medico.setIdUsuario(3L);
        medico.setNombre("Medico Test");
        medico.setRol("MEDICO");
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

    @Test
    @DisplayName("Generar turnos recurrentes DIARIO sin solapamientos genera todos los turnos con idRecurrencia")
    void generarTurnosRecurrentes_DiarioSinConflictos() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 1, 8, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 11, 1, 12, 0);
        LocalDate limite = LocalDate.of(2026, 11, 3); // 1, 2 y 3 de nov -> 3 turnos

        TurnoRecurrenteRequestDTO request = new TurnoRecurrenteRequestDTO(
                1L, 2L, 3L, inicio, fin, "DIARIO", limite, "ASIGNADO"
        );

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(cuidador));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(medico));
        when(turnoRepository.existeSolapamientoCuidador(anyLong(), any(), any())).thenReturn(false);
        when(turnoRepository.existeSolapamientoPaciente(anyLong(), any(), any())).thenReturn(false);
        when(turnoRepository.save(any(TurnoEntity.class))).thenAnswer(inv -> {
            TurnoEntity t = inv.getArgument(0);
            t.setIdTurno(100L);
            return t;
        });

        TurnoRecurrenteResponseDTO response = turnoService.generarTurnosRecurrentes(request);

        assertNotNull(response);
        assertNotNull(response.getIdRecurrencia());
        assertEquals("DIARIO", response.getPatronRecurrencia());
        assertEquals(3, response.getTotalGenerados());
        assertEquals(0, response.getTotalOmitidosPorConflicto());
        assertEquals(3, response.getTurnosCreados().size());
        assertTrue(response.getTurnosOmitidos().isEmpty());

        verify(turnoRepository, times(3)).save(any(TurnoEntity.class));
    }

    @Test
    @DisplayName("Generar turnos recurrentes omite turnos que tengan conflicto de horario con cuidador")
    void generarTurnosRecurrentes_ConConflictoCuidador() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 1, 8, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 11, 1, 12, 0);
        LocalDate limite = LocalDate.of(2026, 11, 3);

        TurnoRecurrenteRequestDTO request = new TurnoRecurrenteRequestDTO(
                1L, 2L, 3L, inicio, fin, "DIARIO", limite, "ASIGNADO"
        );

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(cuidador));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(medico));

        // El día 2 de noviembre tiene solapamiento con el cuidador
        LocalDateTime inicioDia2 = LocalDateTime.of(2026, 11, 2, 8, 0);
        when(turnoRepository.existeSolapamientoCuidador(eq(2L), any(), any())).thenAnswer(inv -> {
            LocalDateTime ini = inv.getArgument(1);
            return ini.equals(inicioDia2);
        });
        when(turnoRepository.existeSolapamientoPaciente(anyLong(), any(), any())).thenReturn(false);

        when(turnoRepository.save(any(TurnoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        TurnoRecurrenteResponseDTO response = turnoService.generarTurnosRecurrentes(request);

        assertNotNull(response);
        assertEquals(2, response.getTotalGenerados());
        assertEquals(1, response.getTotalOmitidosPorConflicto());
        assertEquals(1, response.getTurnosOmitidos().size());
        assertEquals("Solapamiento con turno existente del cuidador", response.getTurnosOmitidos().get(0).getMotivo());
        assertEquals(inicioDia2, response.getTurnosOmitidos().get(0).getFechaHoraInicio());
    }

    @Test
    @DisplayName("Generar turnos recurrentes que superan 3 meses de anticipación lanza excepción")
    void generarTurnosRecurrentes_SuperaLimiteMaximoMeses() {
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 8, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 1, 1, 12, 0);
        LocalDate limite = LocalDate.of(2026, 5, 1); // 4 meses después

        TurnoRecurrenteRequestDTO request = new TurnoRecurrenteRequestDTO(
                1L, 2L, 3L, inicio, fin, "DIARIO", limite, "ASIGNADO"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> turnoService.generarTurnosRecurrentes(request));

        assertTrue(ex.getMessage().contains("límite máximo de 3 meses"));
    }

    @Test
    @DisplayName("Generar turnos recurrentes con patrón inválido lanza excepción")
    void generarTurnosRecurrentes_PatronInvalido() {
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 8, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 1, 1, 12, 0);
        LocalDate limite = LocalDate.of(2026, 1, 15);

        TurnoRecurrenteRequestDTO request = new TurnoRecurrenteRequestDTO(
                1L, 2L, 3L, inicio, fin, "QUINCENAL", limite, "ASIGNADO"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> turnoService.generarTurnosRecurrentes(request));

        assertTrue(ex.getMessage().contains("Patrón de recurrencia inválido"));
    }

    @Test
    @DisplayName("Obtener turnos por recurrencia retorna la lista vinculada al idRecurrencia")
    void obtenerTurnosPorRecurrencia_Exitoso() {
        String idRecurrencia = "rec-123-uuid";
        TurnoEntity turno = new TurnoEntity();
        turno.setIdTurno(10L);
        turno.setPaciente(paciente);
        turno.setCuidador(cuidador);
        turno.setMedico(medico);
        turno.setIdRecurrencia(idRecurrencia);

        when(turnoRepository.findByIdRecurrencia(idRecurrencia)).thenReturn(List.of(turno));

        List<TurnoResponseDTO> lista = turnoService.obtenerTurnosPorRecurrencia(idRecurrencia);

        assertEquals(1, lista.size());
        assertEquals("rec-123-uuid", lista.get(0).getIdRecurrencia());
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

        PacienteEntity pacienteTest = new PacienteEntity();
        pacienteTest.setIdPaciente(1L);
        pacienteTest.setNombreCompleto("Paciente Uno");

        TurnoEntity turno1 = new TurnoEntity();
        turno1.setIdTurno(101L);
        turno1.setPaciente(pacienteTest);
        turno1.setCuidador(profesional);
        turno1.setFechaHoraInicio(LocalDateTime.of(2026, 5, 10, 10, 0));
        turno1.setFechaHoraFin(LocalDateTime.of(2026, 5, 10, 12, 0));
        turno1.setEstado("CONFIRMADO");

        TurnoEntity turno2 = new TurnoEntity();
        turno2.setIdTurno(102L);
        turno2.setPaciente(pacienteTest);
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

    private TurnoEntity crearTurnoEntityBase(Long idTurno, String estado) {
        PacienteEntity p = new PacienteEntity();
        p.setIdPaciente(1L);
        p.setNombreCompleto("Paciente Test");

        UsuarioEntity c = new UsuarioEntity();
        c.setIdUsuario(2L);
        c.setNombre("Cuidador Test");

        UsuarioEntity m = new UsuarioEntity();
        m.setIdUsuario(3L);
        m.setNombre("Dr. Perez");
        m.setRol("MEDICO");

        TurnoEntity turno = new TurnoEntity();
        turno.setIdTurno(idTurno);
        turno.setPaciente(p);
        turno.setCuidador(c);
        turno.setMedico(m);
        turno.setFechaHoraInicio(LocalDateTime.of(2026, 1, 15, 10, 0));
        turno.setFechaHoraFin(LocalDateTime.of(2026, 1, 15, 11, 0));
        turno.setEstado(estado);
        return turno;
    }
}
