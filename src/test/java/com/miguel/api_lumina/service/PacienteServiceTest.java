package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AsignacionProfesionalResponseDTO;
import com.miguel.api_lumina.dto.HistorialDTO;
import com.miguel.api_lumina.dto.PacienteInactivoResponseDTO;
import com.miguel.api_lumina.dto.PacienteResponseDTO;
import com.miguel.api_lumina.entity.AsignacionProfesionalPacienteEntity;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.entity.TipoAsignacionProfesional;
import com.miguel.api_lumina.entity.TratamientoEntity;
import com.miguel.api_lumina.entity.TurnoEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.AsignacionProfesionalPacienteRepository;
import com.miguel.api_lumina.repository.PacienteRepository;
import com.miguel.api_lumina.repository.TratamientoRepository;
import com.miguel.api_lumina.repository.TurnoRepository;
import com.miguel.api_lumina.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private TratamientoRepository tratamientoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AsignacionProfesionalPacienteRepository asignacionProfesionalPacienteRepository;

    @InjectMocks
    private PacienteService pacienteService;

    private PacienteEntity paciente;
    private UsuarioEntity cuidador;
    private UsuarioEntity medico;

    @BeforeEach
    void setUp() {
        paciente = new PacienteEntity();
        paciente.setIdPaciente(1L);
        paciente.setNombreCompleto("Paciente Test");
        paciente.setFechaNacimiento(LocalDate.of(2000, 5, 10));
        paciente.setDiagnosticoFase("Fase inicial");
        paciente.setActivo(true);

        cuidador = new UsuarioEntity();
        cuidador.setIdUsuario(2L);
        cuidador.setNombre("Cuidador Test");
        cuidador.setEmail("cuidador@test.com");
        cuidador.setRol("CUIDADOR");
        cuidador.setActivo(true);

        medico = new UsuarioEntity();
        medico.setIdUsuario(3L);
        medico.setNombre("Medico Test");
        medico.setEmail("medico@test.com");
        medico.setRol("MEDICO");
        medico.setActivo(true);
    }

    @Test
    @DisplayName("Debe reactivar un paciente inactivo exitosamente")
    void reactivarPaciente_Exitoso() {
        PacienteEntity inactivo = new PacienteEntity();
        inactivo.setIdPaciente(1L);
        inactivo.setNombreCompleto("Paciente Inactivo");
        inactivo.setActivo(false);
        inactivo.setMotivoBaja("Traslado temporal");
        inactivo.setFechaBaja(LocalDateTime.now().minusDays(5));
        inactivo.setUsuarioBaja("admin@lumina.com");

        when(pacienteRepository.findByIdAnyStatus(1L)).thenReturn(Optional.of(inactivo));

        PacienteResponseDTO response = pacienteService.reactivarPaciente(1L);

        assertNotNull(response);
        assertTrue(response.getActivo());
        assertNull(response.getMotivoBaja());
        verify(pacienteRepository).reactivarPacienteById(1L);
    }

    @Test
    @DisplayName("Reactivar paciente inexistente lanza 404 NOT_FOUND")
    void reactivarPaciente_Inexistente_Lanza404() {
        when(pacienteRepository.findByIdAnyStatus(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                pacienteService.reactivarPaciente(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(pacienteRepository, never()).reactivarPacienteById(anyLong());
    }

    @Test
    @DisplayName("Reactivar paciente que ya está activo lanza 400 BAD_REQUEST")
    void reactivarPaciente_YaActivo_Lanza400() {
        when(pacienteRepository.findByIdAnyStatus(1L)).thenReturn(Optional.of(paciente));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                pacienteService.reactivarPaciente(1L)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(pacienteRepository, never()).reactivarPacienteById(anyLong());
    }

    @Test
    @DisplayName("Obtener pacientes inactivos retorna lista con campos de auditoría")
    void obtenerPacientesInactivos_RetornaLista() {
        PacienteEntity inactivo = new PacienteEntity();
        inactivo.setIdPaciente(5L);
        inactivo.setNombreCompleto("Carlos Baja");
        inactivo.setActivo(false);
        inactivo.setMotivoBaja("Fallecimiento");
        inactivo.setFechaBaja(LocalDateTime.now());
        inactivo.setUsuarioBaja("admin@lumina.com");

        when(pacienteRepository.findPacientesInactivos()).thenReturn(List.of(inactivo));

        List<PacienteInactivoResponseDTO> lista = pacienteService.obtenerPacientesInactivos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals("Carlos Baja", lista.get(0).getNombreCompleto());
        assertEquals("Fallecimiento", lista.get(0).getMotivoBaja());
        assertEquals("admin@lumina.com", lista.get(0).getUsuarioBaja());
    }

    @Test
    @DisplayName("Dar de baja paciente guarda campos de auditoría")
    void darDeBajaPaciente_ConAuditoria() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        pacienteService.darDeBajaPaciente(1L, "Cambio de residencia", "admin@lumina.com");

        verify(pacienteRepository).darDeBajaConAuditoria(
                eq(1L),
                eq("Cambio de residencia"),
                any(LocalDateTime.class),
                eq("admin@lumina.com")
        );
    }

    @Test
    @DisplayName("Debe obtener el historial completo de un paciente")
    void obtenerHistorial_exitoso() {
        TurnoEntity turno = new TurnoEntity();
        turno.setIdTurno(10L);
        turno.setPaciente(paciente);
        turno.setCuidador(cuidador);
        turno.setFechaHoraInicio(LocalDateTime.now().minusDays(2));
        turno.setFechaHoraFin(LocalDateTime.now().minusDays(2).plusHours(1));
        turno.setEstado("FINALIZADO");

        TratamientoEntity tratamiento = new TratamientoEntity();
        tratamiento.setIdTratamiento(20L);
        tratamiento.setPaciente(paciente);
        tratamiento.setMedico(medico);
        tratamiento.setDescripcion("Tratamiento de prueba");
        tratamiento.setFechaInicio(LocalDate.now().minusDays(10));
        tratamiento.setFechaFin(LocalDate.now().plusDays(10));
        tratamiento.setActivo(true);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(turnoRepository.findByPaciente_IdPaciente(1L)).thenReturn(List.of(turno));
        when(tratamientoRepository.findByPaciente_IdPaciente(1L)).thenReturn(List.of(tratamiento));
        when(usuarioRepository.findAllById(any())).thenReturn(List.of(cuidador, medico));

        HistorialDTO resultado = pacienteService.obtenerHistorial(1L);

        assertNotNull(resultado);
        assertNotNull(resultado.getPaciente());
        assertEquals("Paciente Test", resultado.getPaciente().getNombreCompleto());
        assertEquals(1, resultado.getTurnos().size());
        assertEquals(10L, resultado.getTurnos().get(0).getIdTurno());
        assertEquals(1, resultado.getTratamientos().size());
        assertEquals(20L, resultado.getTratamientos().get(0).getIdTratamiento());
        assertEquals(2, resultado.getUsuariosRelacionados().size());

        verify(pacienteRepository, times(1)).findById(1L);
        verify(turnoRepository, times(1)).findByPaciente_IdPaciente(1L);
        verify(tratamientoRepository, times(1)).findByPaciente_IdPaciente(1L);
        verify(usuarioRepository, times(1)).findAllById(any());
    }

    @Test
    @DisplayName("Debe fallar si el paciente no existe")
    void obtenerHistorial_pacienteInexistente() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> pacienteService.obtenerHistorial(99L));

        verify(turnoRepository, never()).findByPaciente_IdPaciente(anyLong());
        verify(tratamientoRepository, never()).findByPaciente_IdPaciente(anyLong());
        verify(usuarioRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("B-25 - Debe asignar correctamente un médico de cabecera")
    void asignarMedicoCabecera_Exitoso() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(medico));

        when(asignacionProfesionalPacienteRepository
                .findFirstByPaciente_IdPacienteAndTipoAsignacionAndActivaTrue(
                        1L,
                        TipoAsignacionProfesional.MEDICO_CABECERA
                ))
                .thenReturn(Optional.empty());

        when(asignacionProfesionalPacienteRepository
                .save(any(AsignacionProfesionalPacienteEntity.class)))
                .thenAnswer(invocation -> {
                    AsignacionProfesionalPacienteEntity asignacion = invocation.getArgument(0);
                    asignacion.setIdAsignacion(100L);
                    return asignacion;
                });

        AsignacionProfesionalResponseDTO response =
                pacienteService.asignarMedicoCabecera(1L, 3L);

        assertNotNull(response);
        assertEquals(100L, response.getIdAsignacion());
        assertEquals(1L, response.getIdPaciente());
        assertEquals(3L, response.getIdUsuario());
        assertEquals("Medico Test", response.getNombreUsuario());
        assertEquals(TipoAsignacionProfesional.MEDICO_CABECERA, response.getTipoAsignacion());
        assertTrue(response.getActiva());
        assertNotNull(response.getFechaDesde());
        assertNull(response.getFechaHasta());

        verify(asignacionProfesionalPacienteRepository)
                .save(any(AsignacionProfesionalPacienteEntity.class));
    }

    @Test
    @DisplayName("B-25 - Rechaza asignar como médico a un usuario que no tiene rol MEDICO")
    void asignarMedicoCabecera_RolIncorrecto() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(cuidador));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pacienteService.asignarMedicoCabecera(1L, 2L)
        );

        assertEquals("El usuario indicado no tiene rol MEDICO", ex.getMessage());

        verify(asignacionProfesionalPacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("B-25 - Reemplazar médico conserva la asignación anterior como inactiva")
    void asignarMedicoCabecera_ReemplazaAnterior() {
        UsuarioEntity medicoAnterior = new UsuarioEntity();
        medicoAnterior.setIdUsuario(4L);
        medicoAnterior.setNombre("Medico Anterior");
        medicoAnterior.setRol("MEDICO");
        medicoAnterior.setActivo(true);

        AsignacionProfesionalPacienteEntity asignacionAnterior =
                new AsignacionProfesionalPacienteEntity();

        asignacionAnterior.setIdAsignacion(50L);
        asignacionAnterior.setPaciente(paciente);
        asignacionAnterior.setUsuario(medicoAnterior);
        asignacionAnterior.setTipoAsignacion(TipoAsignacionProfesional.MEDICO_CABECERA);
        asignacionAnterior.setFechaDesde(LocalDateTime.now().minusMonths(2));
        asignacionAnterior.setActiva(true);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(medico));

        when(asignacionProfesionalPacienteRepository
                .findFirstByPaciente_IdPacienteAndTipoAsignacionAndActivaTrue(
                        1L,
                        TipoAsignacionProfesional.MEDICO_CABECERA
                ))
                .thenReturn(Optional.of(asignacionAnterior));

        when(asignacionProfesionalPacienteRepository
                .save(any(AsignacionProfesionalPacienteEntity.class)))
                .thenAnswer(invocation -> {
                    AsignacionProfesionalPacienteEntity asignacion = invocation.getArgument(0);

                    if (asignacion.getIdAsignacion() == null) {
                        asignacion.setIdAsignacion(101L);
                    }

                    return asignacion;
                });

        AsignacionProfesionalResponseDTO response =
                pacienteService.asignarMedicoCabecera(1L, 3L);

        assertNotNull(response);
        assertFalse(asignacionAnterior.getActiva());
        assertNotNull(asignacionAnterior.getFechaHasta());
        assertEquals(3L, response.getIdUsuario());
        assertTrue(response.getActiva());

        verify(asignacionProfesionalPacienteRepository, times(2))
                .save(any(AsignacionProfesionalPacienteEntity.class));
    }

    @Test
    @DisplayName("B-25 - Reasignar el mismo médico no crea un registro duplicado")
    void asignarMedicoCabecera_MismoMedico_NoDuplica() {
        AsignacionProfesionalPacienteEntity actual =
                new AsignacionProfesionalPacienteEntity();

        actual.setIdAsignacion(40L);
        actual.setPaciente(paciente);
        actual.setUsuario(medico);
        actual.setTipoAsignacion(TipoAsignacionProfesional.MEDICO_CABECERA);
        actual.setFechaDesde(LocalDateTime.now().minusDays(5));
        actual.setActiva(true);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(medico));

        when(asignacionProfesionalPacienteRepository
                .findFirstByPaciente_IdPacienteAndTipoAsignacionAndActivaTrue(
                        1L,
                        TipoAsignacionProfesional.MEDICO_CABECERA
                ))
                .thenReturn(Optional.of(actual));

        AsignacionProfesionalResponseDTO response =
                pacienteService.asignarMedicoCabecera(1L, 3L);

        assertNotNull(response);
        assertEquals(40L, response.getIdAsignacion());

        verify(asignacionProfesionalPacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("B-25 - Debe agregar correctamente un cuidador")
    void agregarCuidador_Exitoso() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(cuidador));

        when(asignacionProfesionalPacienteRepository
                .existsByPaciente_IdPacienteAndUsuario_IdUsuarioAndTipoAsignacionAndActivaTrue(
                        1L,
                        2L,
                        TipoAsignacionProfesional.CUIDADOR
                ))
                .thenReturn(false);

        when(asignacionProfesionalPacienteRepository
                .save(any(AsignacionProfesionalPacienteEntity.class)))
                .thenAnswer(invocation -> {
                    AsignacionProfesionalPacienteEntity asignacion = invocation.getArgument(0);
                    asignacion.setIdAsignacion(200L);
                    return asignacion;
                });

        AsignacionProfesionalResponseDTO response =
                pacienteService.agregarCuidador(1L, 2L);

        assertNotNull(response);
        assertEquals(200L, response.getIdAsignacion());
        assertEquals(1L, response.getIdPaciente());
        assertEquals(2L, response.getIdUsuario());
        assertEquals("Cuidador Test", response.getNombreUsuario());
        assertEquals(TipoAsignacionProfesional.CUIDADOR, response.getTipoAsignacion());
        assertTrue(response.getActiva());

        verify(asignacionProfesionalPacienteRepository)
                .save(any(AsignacionProfesionalPacienteEntity.class));
    }

    @Test
    @DisplayName("B-25 - Rechaza asignar como cuidador a un usuario que no tiene rol CUIDADOR")
    void agregarCuidador_RolIncorrecto() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(medico));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pacienteService.agregarCuidador(1L, 3L)
        );

        assertEquals("El usuario indicado no tiene rol CUIDADOR", ex.getMessage());

        verify(asignacionProfesionalPacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("B-25 - No permite asignar dos veces el mismo cuidador activo")
    void agregarCuidador_YaAsignado() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(cuidador));

        when(asignacionProfesionalPacienteRepository
                .existsByPaciente_IdPacienteAndUsuario_IdUsuarioAndTipoAsignacionAndActivaTrue(
                        1L,
                        2L,
                        TipoAsignacionProfesional.CUIDADOR
                ))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pacienteService.agregarCuidador(1L, 2L)
        );

        assertEquals(
                "El cuidador ya se encuentra asignado al paciente",
                ex.getMessage()
        );

        verify(asignacionProfesionalPacienteRepository, never()).save(any());
    }
}