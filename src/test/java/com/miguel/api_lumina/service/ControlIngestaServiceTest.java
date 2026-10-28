package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.ControlIngestaRequestDTO;
import com.miguel.api_lumina.dto.ControlIngestaResponseDTO;
import com.miguel.api_lumina.dto.ResumenIngestaResponseDTO;
import com.miguel.api_lumina.entity.ControlIngestaEntity;
import com.miguel.api_lumina.entity.ReporteDiarioEntity;
import com.miguel.api_lumina.repository.ControlIngestaRepository;
import com.miguel.api_lumina.repository.ReporteDiarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControlIngestaServiceTest {

    @Mock
    private ControlIngestaRepository controlIngestaRepository;

    @Mock
    private ReporteDiarioRepository reporteDiarioRepository;

    @InjectMocks
    private ControlIngestaService controlIngestaService;

    private ReporteDiarioEntity reporte;

    @BeforeEach
    void setUp() {
        reporte = new ReporteDiarioEntity();
        reporte.setIdReporte(1L);
    }

    @Test
    @DisplayName("Registrar control de ingesta exitosamente retorna 201 DTO")
    void registrarIngesta_Exitoso() {
        ControlIngestaRequestDTO request = new ControlIngestaRequestDTO(
                "ALMUERZO",
                "Pechuga de pollo con puré de calabaza",
                85,
                350,
                LocalDateTime.now(),
                "Comió con buen apetito"
        );

        when(reporteDiarioRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(controlIngestaRepository.save(any(ControlIngestaEntity.class))).thenAnswer(invocation -> {
            ControlIngestaEntity entity = invocation.getArgument(0);
            entity.setIdControlIngesta(10L);
            return entity;
        });

        ControlIngestaResponseDTO response = controlIngestaService.registrarIngesta(1L, request);

        assertNotNull(response);
        assertEquals(10L, response.getIdControlIngesta());
        assertEquals(1L, response.getIdReporte());
        assertEquals("ALMUERZO", response.getTipoComida());
        assertEquals("Pechuga de pollo con puré de calabaza", response.getDescripcionComida());
        assertEquals(85, response.getPorcentajeConsumido());
        assertEquals(350, response.getAguaMl());
        assertTrue(response.getActivo());

        verify(controlIngestaRepository, times(1)).save(any(ControlIngestaEntity.class));
    }

    @Test
    @DisplayName("Registrar ingesta con reporte inexistente lanza 404 NOT_FOUND")
    void registrarIngesta_ReporteInexistente_Lanza404() {
        ControlIngestaRequestDTO request = new ControlIngestaRequestDTO(
                "ALMUERZO", "Alimento", 80, 200, LocalDateTime.now(), null
        );

        when(reporteDiarioRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controlIngestaService.registrarIngesta(999L, request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("El reporte diario con ID 999 no existe"));
    }

    @Test
    @DisplayName("Registrar ingesta con porcentaje mayor a 100 lanza 400 BAD_REQUEST")
    void registrarIngesta_PorcentajeMayorA100_Lanza400() {
        ControlIngestaRequestDTO request = new ControlIngestaRequestDTO(
                "ALMUERZO", "Alimento", 120, 200, LocalDateTime.now(), null
        );

        when(reporteDiarioRepository.findById(1L)).thenReturn(Optional.of(reporte));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controlIngestaService.registrarIngesta(1L, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("porcentaje"));
    }

    @Test
    @DisplayName("Registrar ingesta con agua negativa lanza 400 BAD_REQUEST")
    void registrarIngesta_AguaNegativa_Lanza400() {
        ControlIngestaRequestDTO request = new ControlIngestaRequestDTO(
                "ALMUERZO", "Alimento", 80, -50, LocalDateTime.now(), null
        );

        when(reporteDiarioRepository.findById(1L)).thenReturn(Optional.of(reporte));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controlIngestaService.registrarIngesta(1L, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("agua"));
    }

    @Test
    @DisplayName("Obtener ingestas por reporte exitoso")
    void obtenerIngestasPorReporte_Exitoso() {
        ControlIngestaEntity ingesta = new ControlIngestaEntity(
                1L, reporte, "DESAYUNO", "Tostadas con queso", 100, 250, LocalDateTime.now(), null, true
        );

        when(reporteDiarioRepository.existsById(1L)).thenReturn(true);
        when(controlIngestaRepository.findByReporte_IdReporte(1L)).thenReturn(List.of(ingesta));

        List<ControlIngestaResponseDTO> lista = controlIngestaService.obtenerIngestasPorReporte(1L);

        assertEquals(1, lista.size());
        assertEquals("DESAYUNO", lista.get(0).getTipoComida());
        assertEquals(250, lista.get(0).getAguaMl());
    }

    @Test
    @DisplayName("Obtener resumen consolidado calcula total de agua y promedio de ingesta nutricional")
    void obtenerResumenIngestaPorReporte_CalculaTotalesCorrectos() {
        ControlIngestaEntity ingesta1 = new ControlIngestaEntity(
                1L, reporte, "DESAYUNO", "Té y galletas", 100, 300, LocalDateTime.now(), null, true
        );
        ControlIngestaEntity ingesta2 = new ControlIngestaEntity(
                2L, reporte, "ALMUERZO", "Arroz con verduras", 70, 500, LocalDateTime.now(), null, true
        );

        when(reporteDiarioRepository.existsById(1L)).thenReturn(true);
        when(controlIngestaRepository.findByReporte_IdReporte(1L)).thenReturn(List.of(ingesta1, ingesta2));

        ResumenIngestaResponseDTO resumen = controlIngestaService.obtenerResumenIngestaPorReporte(1L);

        assertNotNull(resumen);
        assertEquals(1L, resumen.getIdReporte());
        assertEquals(800, resumen.getTotalAguaMl()); // 300 + 500
        assertEquals(2, resumen.getCantidadIngestas());
        assertEquals(85.0, resumen.getPorcentajeConsumoPromedio()); // (100 + 70) / 2 = 85.0
        assertEquals(2, resumen.getIngestas().size());
    }

    @Test
    @DisplayName("Obtener ingesta por id existente")
    void obtenerIngestaPorId_Exitoso() {
        ControlIngestaEntity ingesta = new ControlIngestaEntity(
                5L, reporte, "CENA", "Sopa", 90, 400, LocalDateTime.now(), "Tranquilo", true
        );

        when(controlIngestaRepository.findById(5L)).thenReturn(Optional.of(ingesta));

        ControlIngestaResponseDTO response = controlIngestaService.obtenerIngestaPorId(5L);

        assertNotNull(response);
        assertEquals(5L, response.getIdControlIngesta());
        assertEquals(400, response.getAguaMl());
    }

    @Test
    @DisplayName("Obtener ingesta por id inexistente lanza 404")
    void obtenerIngestaPorId_Inexistente_Lanza404() {
        when(controlIngestaRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controlIngestaService.obtenerIngestaPorId(999L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
