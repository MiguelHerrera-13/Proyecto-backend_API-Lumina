package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.TratamientoRequestDTO;
import com.miguel.api_lumina.dto.TratamientoResponseDTO;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.entity.TratamientoEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.PacienteRepository;
import com.miguel.api_lumina.repository.TratamientoRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TratamientoServiceTest {

    @Mock
    private TratamientoRepository tratamientoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TratamientoService tratamientoService;

    private PacienteEntity paciente;
    private UsuarioEntity medico;
    private TratamientoRequestDTO requestValido;

    @BeforeEach
    void setUp() {
        paciente = new PacienteEntity();
        paciente.setIdPaciente(1L);
        paciente.setNombreCompleto("Juan Perez");

        medico = new UsuarioEntity();
        medico.setIdUsuario(2L);
        medico.setNombre("Dr. Lopez");
        medico.setRol("MEDICO");

        requestValido = new TratamientoRequestDTO();
        requestValido.setIdPaciente(1L);
        requestValido.setIdMedico(2L);
        requestValido.setDescripcion("Paracetamol 500mg cada 8 horas por 7 días");
        requestValido.setFechaInicio(LocalDate.now());
        requestValido.setFechaFin(LocalDate.now().plusDays(7));
    }

    @Test
    @DisplayName("Debe registrar un tratamiento exitosamente cuando los datos son válidos")
    void registrarTratamiento_exitoso() {
        // Given
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(medico));
        when(tratamientoRepository.save(any(TratamientoEntity.class))).thenAnswer(invocation -> {
            TratamientoEntity t = invocation.getArgument(0);
            t.setIdTratamiento(100L);
            return t;
        });

        // When
        TratamientoResponseDTO resultado = tratamientoService.registrarTratamiento(requestValido);

        // Then
        assertNotNull(resultado);
        assertEquals(100L, resultado.getIdTratamiento());
        assertEquals(1L, resultado.getIdPaciente());
        assertEquals("Juan Perez", resultado.getNombrePaciente());
        assertEquals(2L, resultado.getIdMedico());
        assertEquals("Dr. Lopez", resultado.getNombreMedico());
        assertEquals("Paracetamol 500mg cada 8 horas por 7 días", resultado.getDescripcion());
        assertTrue(resultado.getActivo());
        verify(tratamientoRepository, times(1)).save(any(TratamientoEntity.class));
    }

    @Test
    @DisplayName("Debe fallar al registrar si fecha_fin es anterior o igual a fecha_inicio")
    void registrarTratamiento_fechaFinInvalida_lanzaExcepcion() {
        // Given: fechaFin igual a fechaInicio
        requestValido.setFechaFin(requestValido.getFechaInicio());

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            tratamientoService.registrarTratamiento(requestValido);
        });

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("La fecha de fin debe ser posterior a la fecha de inicio"));
        verify(tratamientoRepository, never()).save(any(TratamientoEntity.class));

        // Given: fechaFin anterior a fechaInicio
        requestValido.setFechaFin(requestValido.getFechaInicio().minusDays(1));
        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, () -> {
            tratamientoService.registrarTratamiento(requestValido);
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatusCode());
    }

    @Test
    @DisplayName("Debe fallar al registrar si el paciente no existe")
    void registrarTratamiento_pacienteNoExiste_lanzaExcepcion() {
        // Given
        when(pacienteRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            tratamientoService.registrarTratamiento(requestValido);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("El paciente con ID 1 no existe"));
        verify(tratamientoRepository, never()).save(any(TratamientoEntity.class));
    }

    @Test
    @DisplayName("Debe fallar al registrar si el médico no existe")
    void registrarTratamiento_medicoNoExiste_lanzaExcepcion() {
        // Given
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            tratamientoService.registrarTratamiento(requestValido);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("El médico con ID 2 no existe"));
        verify(tratamientoRepository, never()).save(any(TratamientoEntity.class));
    }

    @Test
    @DisplayName("Debe fallar al registrar si el usuario no tiene rol MEDICO")
    void registrarTratamiento_usuarioNoEsMedico_lanzaExcepcion() {
        // Given
        UsuarioEntity cuidador = new UsuarioEntity();
        cuidador.setIdUsuario(3L);
        cuidador.setNombre("Carlos Cuidador");
        cuidador.setRol("CUIDADOR");

        requestValido.setIdMedico(3L);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(cuidador));

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            tratamientoService.registrarTratamiento(requestValido);
        });

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("no tiene el rol de MEDICO"));
        verify(tratamientoRepository, never()).save(any(TratamientoEntity.class));
    }

    @Test
    @DisplayName("Debe obtener el historial de tratamientos por paciente existente")
    void obtenerTratamientosPorPaciente_exitoso() {
        // Given
        TratamientoEntity t1 = new TratamientoEntity(10L, paciente, medico, "Tratamiento 1", LocalDate.now(), LocalDate.now().plusDays(5), true);
        TratamientoEntity t2 = new TratamientoEntity(11L, paciente, medico, "Tratamiento 2", LocalDate.now(), LocalDate.now().plusDays(10), true);

        when(pacienteRepository.existsById(1L)).thenReturn(true);
        when(tratamientoRepository.findByPaciente_IdPaciente(1L)).thenReturn(List.of(t1, t2));

        // When
        List<TratamientoResponseDTO> resultado = tratamientoService.obtenerTratamientosPorPaciente(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(10L, resultado.get(0).getIdTratamiento());
        assertEquals("Tratamiento 1", resultado.get(0).getDescripcion());
        assertEquals(11L, resultado.get(1).getIdTratamiento());
        assertEquals("Tratamiento 2", resultado.get(1).getDescripcion());
    }

    @Test
    @DisplayName("Debe fallar al obtener tratamientos si el paciente no existe")
    void obtenerTratamientosPorPaciente_noExiste_lanzaExcepcion() {
        // Given
        when(pacienteRepository.existsById(99L)).thenReturn(false);

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            tratamientoService.obtenerTratamientosPorPaciente(99L);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("El paciente con ID 99 no existe"));
    }

    @Test
    @DisplayName("Debe dar de baja lógica un tratamiento existente")
    void darDeBajaTratamiento_exitoso() {
        // Given
        TratamientoEntity tratamiento = new TratamientoEntity(10L, paciente, medico, "Tratamiento", LocalDate.now(), LocalDate.now().plusDays(5), true);
        when(tratamientoRepository.findById(10L)).thenReturn(Optional.of(tratamiento));

        // When
        tratamientoService.darDeBajaTratamiento(10L);

        // Then
        verify(tratamientoRepository, times(1)).delete(tratamiento);
    }
}
