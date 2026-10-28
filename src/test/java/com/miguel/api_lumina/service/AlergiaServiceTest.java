package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AlergiaRequestDTO;
import com.miguel.api_lumina.dto.AlergiaResponseDTO;
import com.miguel.api_lumina.entity.AlergiaEntity;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.repository.AlergiaRepository;
import com.miguel.api_lumina.repository.PacienteRepository;
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
class AlergiaServiceTest {

    @Mock
    private AlergiaRepository alergiaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private AlergiaService alergiaService;

    private PacienteEntity paciente;
    private AlergiaRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        paciente = new PacienteEntity();
        paciente.setIdPaciente(1L);
        paciente.setNombreCompleto("María López");

        requestDTO = new AlergiaRequestDTO("Penicilina", "GRAVE", "Shock anafiláctico");
    }

    @Test
    @DisplayName("Debe registrar una alergia exitosamente con severidad válida")
    void testCrearAlergia_Exito() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(alergiaRepository.existsByPacienteIdPacienteAndTipoIgnoreCase(1L, "Penicilina")).thenReturn(false);

        AlergiaEntity guardada = new AlergiaEntity(paciente, "Penicilina", "GRAVE", "Shock anafiláctico");
        guardada.setIdAlergia(10L);

        when(alergiaRepository.save(any(AlergiaEntity.class))).thenReturn(guardada);

        AlergiaResponseDTO response = alergiaService.crearAlergia(1L, requestDTO);

        assertNotNull(response);
        assertEquals(10L, response.getIdAlergia());
        assertEquals(1L, response.getIdPaciente());
        assertEquals("Penicilina", response.getTipo());
        assertEquals("GRAVE", response.getSeveridad());
        assertEquals("Shock anafiláctico", response.getDescripcion());
        verify(alergiaRepository, times(1)).save(any(AlergiaEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar 404 si el paciente no existe al crear alergia")
    void testCrearAlergia_PacienteNoExiste_Lanza404() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                alergiaService.crearAlergia(99L, requestDTO)
        );

        assertTrue(ex.getMessage().contains("Paciente no encontrado"));
        verify(alergiaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar 409 si ya existe una alergia activa del mismo tipo para el paciente")
    void testCrearAlergia_Duplicada_Lanza409() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(alergiaRepository.existsByPacienteIdPacienteAndTipoIgnoreCase(1L, "Penicilina")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                alergiaService.crearAlergia(1L, requestDTO)
        );

        assertTrue(ex.getMessage().contains("Ya existe una alergia activa"));
        verify(alergiaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar 400 si la severidad no es LEVE, MODERADA o GRAVE")
    void testCrearAlergia_SeveridadInvalida_Lanza400() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        requestDTO.setSeveridad("EXTREMA"); // No válida

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                alergiaService.crearAlergia(1L, requestDTO)
        );

        assertTrue(ex.getMessage().contains("Severidad inválida"));
        verify(alergiaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe retornar lista completa de alergias activas del paciente")
    void testListarAlergiasPorPaciente_Exito() {
        when(pacienteRepository.existsById(1L)).thenReturn(true);

        AlergiaEntity a1 = new AlergiaEntity(paciente, "Penicilina", "GRAVE", "Erupción");
        a1.setIdAlergia(1L);
        AlergiaEntity a2 = new AlergiaEntity(paciente, "Polen", "LEVE", "Rinitis");
        a2.setIdAlergia(2L);

        when(alergiaRepository.findByPacienteIdPaciente(1L)).thenReturn(List.of(a1, a2));

        List<AlergiaResponseDTO> lista = alergiaService.listarAlergiasPorPaciente(1L);

        assertNotNull(lista);
        assertEquals(2, lista.size());
        assertEquals("Penicilina", lista.get(0).getTipo());
        assertEquals("Polen", lista.get(1).getTipo());
    }

    @Test
    @DisplayName("Debe obtener alergia por ID exitosamente")
    void testObtenerAlergiaPorId_Exito() {
        when(pacienteRepository.existsById(1L)).thenReturn(true);

        AlergiaEntity alergia = new AlergiaEntity(paciente, "Aspirina", "MODERADA", "Urticaria");
        alergia.setIdAlergia(5L);

        when(alergiaRepository.findByIdAlergiaAndPacienteIdPaciente(5L, 1L))
                .thenReturn(Optional.of(alergia));

        AlergiaResponseDTO response = alergiaService.obtenerAlergiaPorId(1L, 5L);

        assertNotNull(response);
        assertEquals(5L, response.getIdAlergia());
        assertEquals("Aspirina", response.getTipo());
    }

    @Test
    @DisplayName("Debe actualizar alergia exitosamente")
    void testActualizarAlergia_Exito() {
        when(pacienteRepository.existsById(1L)).thenReturn(true);

        AlergiaEntity existente = new AlergiaEntity(paciente, "Penicilina", "LEVE", "Picazón");
        existente.setIdAlergia(3L);

        when(alergiaRepository.findByIdAlergiaAndPacienteIdPaciente(3L, 1L))
                .thenReturn(Optional.of(existente));
        when(alergiaRepository.existsByPacienteIdPacienteAndTipoIgnoreCaseAndIdAlergiaNot(1L, "Penicilina", 3L))
                .thenReturn(false);

        AlergiaEntity actualizada = new AlergiaEntity(paciente, "Penicilina", "MODERADA", "Picazón y enrojecimiento");
        actualizada.setIdAlergia(3L);

        when(alergiaRepository.save(any(AlergiaEntity.class))).thenReturn(actualizada);

        AlergiaRequestDTO updateDTO = new AlergiaRequestDTO("Penicilina", "MODERADA", "Picazón y enrojecimiento");
        AlergiaResponseDTO response = alergiaService.actualizarAlergia(1L, 3L, updateDTO);

        assertNotNull(response);
        assertEquals("MODERADA", response.getSeveridad());
        verify(alergiaRepository, times(1)).save(any(AlergiaEntity.class));
    }

    @Test
    @DisplayName("Debe eliminar alergia correctamente (soft-delete)")
    void testEliminarAlergia_Exito() {
        when(pacienteRepository.existsById(1L)).thenReturn(true);

        AlergiaEntity existente = new AlergiaEntity(paciente, "Polen", "LEVE", "Rinitis");
        existente.setIdAlergia(4L);

        when(alergiaRepository.findByIdAlergiaAndPacienteIdPaciente(4L, 1L))
                .thenReturn(Optional.of(existente));

        alergiaService.eliminarAlergia(1L, 4L);

        verify(alergiaRepository, times(1)).delete(existente);
    }
}
