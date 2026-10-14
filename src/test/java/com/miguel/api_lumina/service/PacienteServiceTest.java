package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.HistorialDTO;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.entity.TratamientoEntity;
import com.miguel.api_lumina.entity.TurnoEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
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
    @DisplayName("Debe obtener el historial completo de un paciente")
    void obtenerHistorial_exitoso() {

        TurnoEntity turno = new TurnoEntity();
        turno.setIdTurno(10L);
        turno.setPaciente(paciente);
        turno.setCuidador(cuidador);
        turno.setFechaHoraInicio(
                LocalDateTime.now().minusDays(2)
        );
        turno.setFechaHoraFin(
                LocalDateTime.now().minusDays(2).plusHours(1)
        );
        turno.setEstado("FINALIZADO");

        TratamientoEntity tratamiento = new TratamientoEntity();
        tratamiento.setIdTratamiento(20L);
        tratamiento.setPaciente(paciente);
        tratamiento.setMedico(medico);
        tratamiento.setDescripcion("Tratamiento de prueba");
        tratamiento.setFechaInicio(LocalDate.now().minusDays(10));
        tratamiento.setFechaFin(LocalDate.now().plusDays(10));
        tratamiento.setActivo(true);

        when(pacienteRepository.findById(1L))
                .thenReturn(Optional.of(paciente));

        when(turnoRepository.findByPaciente_IdPaciente(1L))
                .thenReturn(List.of(turno));

        when(tratamientoRepository.findByPaciente_IdPaciente(1L))
                .thenReturn(List.of(tratamiento));

        when(usuarioRepository.findAllById(any()))
                .thenReturn(List.of(cuidador, medico));

        HistorialDTO resultado =
                pacienteService.obtenerHistorial(1L);

        assertNotNull(resultado);

        assertNotNull(resultado.getPaciente());
        assertEquals(
                "Paciente Test",
                resultado.getPaciente().getNombreCompleto()
        );

        assertEquals(1, resultado.getTurnos().size());
        assertEquals(
                10L,
                resultado.getTurnos().get(0).getIdTurno()
        );

        assertEquals(1, resultado.getTratamientos().size());
        assertEquals(
                20L,
                resultado.getTratamientos().get(0).getIdTratamiento()
        );

        assertEquals(2, resultado.getUsuariosRelacionados().size());

        verify(pacienteRepository, times(1))
                .findById(1L);

        verify(turnoRepository, times(1))
                .findByPaciente_IdPaciente(1L);

        verify(tratamientoRepository, times(1))
                .findByPaciente_IdPaciente(1L);

        verify(usuarioRepository, times(1))
                .findAllById(any());
    }

    @Test
    @DisplayName("Debe fallar si el paciente no existe")
    void obtenerHistorial_pacienteInexistente() {

        when(pacienteRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> pacienteService.obtenerHistorial(99L)
        );

        verify(turnoRepository, never())
                .findByPaciente_IdPaciente(anyLong());

        verify(tratamientoRepository, never())
                .findByPaciente_IdPaciente(anyLong());

        verify(usuarioRepository, never())
                .findAllById(any());
    }
}