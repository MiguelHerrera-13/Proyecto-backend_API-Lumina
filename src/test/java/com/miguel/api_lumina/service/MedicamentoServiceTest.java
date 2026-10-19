package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.MedicamentoRequestDTO;
import com.miguel.api_lumina.dto.MedicamentoResponseDTO;
import com.miguel.api_lumina.entity.MedicamentoEntity;
import com.miguel.api_lumina.entity.TratamientoEntity;
import com.miguel.api_lumina.repository.MedicamentoRepository;
import com.miguel.api_lumina.repository.TratamientoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MedicamentoServiceTest {

    @Mock
    private MedicamentoRepository medicamentoRepository;

    @Mock
    private TratamientoRepository tratamientoRepository;

    @InjectMocks
    private MedicamentoService medicamentoService;

    @Test
    @DisplayName("crearMedicamento con tratamiento existente retorna MedicamentoResponseDTO exitosamente")
    void crearMedicamento_TratamientoExistente_RetornaDTO() {
        TratamientoEntity tratamiento = new TratamientoEntity();
        tratamiento.setIdTratamiento(1L);

        MedicamentoRequestDTO requestDTO = new MedicamentoRequestDTO(1L, "Paracetamol", "500 mg", "Cada 8 horas");

        MedicamentoEntity guardado = new MedicamentoEntity(10L, tratamiento, "Paracetamol", "500 mg", "Cada 8 horas", true);

        when(tratamientoRepository.findById(1L)).thenReturn(Optional.of(tratamiento));
        when(medicamentoRepository.save(any(MedicamentoEntity.class))).thenReturn(guardado);

        MedicamentoResponseDTO resultado = medicamentoService.crearMedicamento(requestDTO);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getIdMedicamento());
        assertEquals(1L, resultado.getIdTratamiento());
        assertEquals("Paracetamol", resultado.getNombre());
        assertEquals("500 mg", resultado.getDosis());
        assertEquals("Cada 8 horas", resultado.getFrecuencia());
        assertTrue(resultado.getActivo());

        verify(tratamientoRepository).findById(1L);
        verify(medicamentoRepository).save(any(MedicamentoEntity.class));
    }

    @Test
    @DisplayName("crearMedicamento con tratamiento inexistente lanza ResponseStatusException 404 NOT_FOUND")
    void crearMedicamento_TratamientoInexistente_Lanza404() {
        MedicamentoRequestDTO requestDTO = new MedicamentoRequestDTO(999L, "Ibuprofeno", "400 mg", "Cada 12 horas");

        when(tratamientoRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                medicamentoService.crearMedicamento(requestDTO));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("El tratamiento con ID 999 no existe"));

        verify(tratamientoRepository).findById(999L);
        verify(medicamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("obtenerMedicamentoPorId con ID existente retorna Optional con DTO")
    void obtenerMedicamentoPorId_Existe_RetornaDTO() {
        TratamientoEntity tratamiento = new TratamientoEntity();
        tratamiento.setIdTratamiento(2L);

        MedicamentoEntity entity = new MedicamentoEntity(5L, tratamiento, "Amoxicilina", "875 mg", "Cada 12 horas", true);

        when(medicamentoRepository.findById(5L)).thenReturn(Optional.of(entity));

        Optional<MedicamentoResponseDTO> resultado = medicamentoService.obtenerMedicamentoPorId(5L);

        assertTrue(resultado.isPresent());
        assertEquals("Amoxicilina", resultado.get().getNombre());
        assertEquals(2L, resultado.get().getIdTratamiento());

        verify(medicamentoRepository).findById(5L);
    }

    @Test
    @DisplayName("obtenerMedicamentoPorId con ID inexistente retorna Optional vacío")
    void obtenerMedicamentoPorId_NoExiste_RetornaVacio() {
        when(medicamentoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<MedicamentoResponseDTO> resultado = medicamentoService.obtenerMedicamentoPorId(99L);

        assertFalse(resultado.isPresent());
        verify(medicamentoRepository).findById(99L);
    }

    @Test
    @DisplayName("obtenerMedicamentosPorTratamiento con tratamiento existente retorna lista de DTOs")
    void obtenerMedicamentosPorTratamiento_TratamientoExiste_RetornaLista() {
        TratamientoEntity tratamiento = new TratamientoEntity();
        tratamiento.setIdTratamiento(1L);

        MedicamentoEntity m1 = new MedicamentoEntity(1L, tratamiento, "Losartan", "50 mg", "Cada 24 horas", true);
        MedicamentoEntity m2 = new MedicamentoEntity(2L, tratamiento, "Aspirina", "100 mg", "Cada 24 horas", true);

        when(tratamientoRepository.existsById(1L)).thenReturn(true);
        when(medicamentoRepository.findByTratamiento_IdTratamiento(1L)).thenReturn(List.of(m1, m2));

        List<MedicamentoResponseDTO> lista = medicamentoService.obtenerMedicamentosPorTratamiento(1L);

        assertNotNull(lista);
        assertEquals(2, lista.size());
        assertEquals("Losartan", lista.get(0).getNombre());
        assertEquals("Aspirina", lista.get(1).getNombre());

        verify(tratamientoRepository).existsById(1L);
        verify(medicamentoRepository).findByTratamiento_IdTratamiento(1L);
    }

    @Test
    @DisplayName("obtenerMedicamentosPorTratamiento con tratamiento inexistente lanza ResponseStatusException 404")
    void obtenerMedicamentosPorTratamiento_TratamientoInexistente_Lanza404() {
        when(tratamientoRepository.existsById(999L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                medicamentoService.obtenerMedicamentosPorTratamiento(999L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(tratamientoRepository).existsById(999L);
        verify(medicamentoRepository, never()).findByTratamiento_IdTratamiento(any());
    }

    @Test
    @DisplayName("actualizarMedicamento con ID existente retorna MedicamentoResponseDTO actualizado")
    void actualizarMedicamento_Existe_RetornaActualizado() {
        TratamientoEntity tratamiento = new TratamientoEntity();
        tratamiento.setIdTratamiento(1L);

        MedicamentoEntity entity = new MedicamentoEntity(1L, tratamiento, "Omeprazol", "20 mg", "Cada 24 horas", true);
        MedicamentoRequestDTO updateDTO = new MedicamentoRequestDTO(1L, "Omeprazol", "40 mg", "Cada 12 horas");

        when(medicamentoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(medicamentoRepository.save(any(MedicamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MedicamentoResponseDTO resultado = medicamentoService.actualizarMedicamento(1L, updateDTO);

        assertNotNull(resultado);
        assertEquals("40 mg", resultado.getDosis());
        assertEquals("Cada 12 horas", resultado.getFrecuencia());

        verify(medicamentoRepository).findById(1L);
        verify(medicamentoRepository).save(entity);
    }

    @Test
    @DisplayName("actualizarMedicamento con ID inexistente lanza ResponseStatusException 404")
    void actualizarMedicamento_NoExiste_Lanza404() {
        MedicamentoRequestDTO updateDTO = new MedicamentoRequestDTO(1L, "Omeprazol", "40 mg", "Cada 12 horas");

        when(medicamentoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                medicamentoService.actualizarMedicamento(99L, updateDTO));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(medicamentoRepository).findById(99L);
        verify(medicamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("darDeBajaMedicamento con ID existente invoca delete en repositorio")
    void darDeBajaMedicamento_Existe_InvocaDelete() {
        TratamientoEntity tratamiento = new TratamientoEntity();
        tratamiento.setIdTratamiento(1L);

        MedicamentoEntity entity = new MedicamentoEntity(1L, tratamiento, "Metformina", "850 mg", "Cada 12 horas", true);

        when(medicamentoRepository.findById(1L)).thenReturn(Optional.of(entity));

        medicamentoService.darDeBajaMedicamento(1L);

        verify(medicamentoRepository).findById(1L);
        verify(medicamentoRepository).delete(entity);
    }

    @Test
    @DisplayName("darDeBajaMedicamento con ID inexistente lanza ResponseStatusException 404")
    void darDeBajaMedicamento_NoExiste_Lanza404() {
        when(medicamentoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                medicamentoService.darDeBajaMedicamento(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(medicamentoRepository).findById(99L);
        verify(medicamentoRepository, never()).delete(any());
    }
}
