package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.SignosVitalesRequestDTO;
import com.miguel.api_lumina.dto.SignosVitalesResponseDTO;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.entity.SignosVitalesEntity;
import com.miguel.api_lumina.repository.PacienteRepository;
import com.miguel.api_lumina.repository.SignosVitalesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignosVitalesServiceTest {

    @Mock
    private SignosVitalesRepository signosVitalesRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private SignosVitalesService signosVitalesService;

    private PacienteEntity paciente;
    private SignosVitalesRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        paciente = new PacienteEntity();
        paciente.setIdPaciente(1L);
        paciente.setNombreCompleto("Juan Pérez");

        requestDTO = new SignosVitalesRequestDTO(
                36.6,
                "120/80",
                72,
                95.0,
                "Enfermero Carlos",
                "Sin anomalías"
        );
    }

    @Test
    @DisplayName("Debe registrar signos vitales exitosamente")
    void testRegistrarSignosVitales_Exito() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        SignosVitalesEntity entityGuardada = new SignosVitalesEntity(
                paciente,
                requestDTO.getTemperatura(),
                requestDTO.getPresionArterial(),
                requestDTO.getFrecuenciaCardiaca(),
                requestDTO.getGlucemia(),
                requestDTO.getAutor(),
                LocalDateTime.now(),
                requestDTO.getObservaciones()
        );
        entityGuardada.setIdSignoVital(10L);

        when(signosVitalesRepository.save(any(SignosVitalesEntity.class))).thenReturn(entityGuardada);

        SignosVitalesResponseDTO response = signosVitalesService.registrarSignosVitales(1L, requestDTO, "usuario@test.com");

        assertNotNull(response);
        assertEquals(10L, response.getIdSignoVital());
        assertEquals(1L, response.getIdPaciente());
        assertEquals(36.6, response.getTemperatura());
        assertEquals("120/80", response.getPresionArterial());
        assertEquals(72, response.getFrecuenciaCardiaca());
        assertEquals(95.0, response.getGlucemia());
        assertEquals("Enfermero Carlos", response.getAutor());
        assertNotNull(response.getFechaHora());

        verify(signosVitalesRepository, times(1)).save(any(SignosVitalesEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar 404 al registrar si el paciente no existe")
    void testRegistrarSignosVitales_PacienteNoExiste_Lanza404() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                signosVitalesService.registrarSignosVitales(99L, requestDTO, "usuario@test.com")
        );

        assertTrue(ex.getMessage().contains("Paciente no encontrado"));
        verify(signosVitalesRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar 400 si la presión sistólica no es mayor a la diastólica")
    void testRegistrarSignosVitales_PresionInvalida_Lanza400() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        requestDTO.setPresionArterial("80/120"); // Sistólica menor que diastólica

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                signosVitalesService.registrarSignosVitales(1L, requestDTO, "usuario@test.com")
        );

        assertTrue(ex.getMessage().contains("presión sistólica"));
        verify(signosVitalesRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener historial ordenado cronológicamente descendente")
    void testObtenerHistorialSignosVitales_Exito() {
        when(pacienteRepository.existsById(1L)).thenReturn(true);

        LocalDateTime ahora = LocalDateTime.now();
        SignosVitalesEntity reg1 = new SignosVitalesEntity(paciente, 37.0, "130/85", 80, 110.0, "Dr. Pérez", ahora, "Control tarde");
        reg1.setIdSignoVital(2L);

        SignosVitalesEntity reg2 = new SignosVitalesEntity(paciente, 36.5, "120/80", 70, 90.0, "Dr. Pérez", ahora.minusHours(4), "Control mañana");
        reg2.setIdSignoVital(1L);

        when(signosVitalesRepository.findByPacienteIdPacienteOrderByFechaHoraDesc(1L))
                .thenReturn(Arrays.asList(reg1, reg2));

        List<SignosVitalesResponseDTO> historial = signosVitalesService.obtenerHistorialSignosVitales(1L);

        assertNotNull(historial);
        assertEquals(2, historial.size());
        assertEquals(2L, historial.get(0).getIdSignoVital());
        assertEquals(1L, historial.get(1).getIdSignoVital());
        assertTrue(historial.get(0).getFechaHora().isAfter(historial.get(1).getFechaHora()));
    }

    @Test
    @DisplayName("Debe lanzar 404 al consultar historial si el paciente no existe")
    void testObtenerHistorialSignosVitales_PacienteNoExiste_Lanza404() {
        when(pacienteRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                signosVitalesService.obtenerHistorialSignosVitales(99L)
        );

        assertTrue(ex.getMessage().contains("Paciente no encontrado"));
        verify(signosVitalesRepository, never()).findByPacienteIdPacienteOrderByFechaHoraDesc(any());
    }
}
