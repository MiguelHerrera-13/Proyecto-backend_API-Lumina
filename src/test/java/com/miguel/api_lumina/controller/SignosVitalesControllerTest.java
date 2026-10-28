package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.SignosVitalesResponseDTO;
import com.miguel.api_lumina.exception.GlobalExceptionHandler;
import com.miguel.api_lumina.service.SignosVitalesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class SignosVitalesControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SignosVitalesService signosVitalesService;

    @InjectMocks
    private SignosVitalesController signosVitalesController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(signosVitalesController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/pacientes/{id}/signos-vitales con datos válidos retorna 201 Created")
    public void testRegistrarSignosVitales_Valido_Retorna201() throws Exception {
        SignosVitalesResponseDTO responseDTO = new SignosVitalesResponseDTO(
                1L, 1L, 36.5, "120/80", 75, 95.0, "Dr. Carlos", LocalDateTime.now(), "Normal"
        );

        when(signosVitalesService.registrarSignosVitales(eq(1L), any(), any()))
                .thenReturn(responseDTO);

        String jsonRequest = """
                {
                    "temperatura": 36.5,
                    "presionArterial": "120/80",
                    "frecuenciaCardiaca": 75,
                    "glucemia": 95.0,
                    "autor": "Dr. Carlos",
                    "observaciones": "Normal"
                }
                """;

        mockMvc.perform(post("/api/pacientes/1/signos-vitales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idSignoVital").value(1))
                .andExpect(jsonPath("$.temperatura").value(36.5))
                .andExpect(jsonPath("$.presionArterial").value("120/80"))
                .andExpect(jsonPath("$.frecuenciaCardiaca").value(75))
                .andExpect(jsonPath("$.glucemia").value(95.0))
                .andExpect(jsonPath("$.autor").value("Dr. Carlos"));
    }

    @Test
    @DisplayName("POST /api/pacientes/{id}/signos-vitales con temperatura fuera de rango retorna 400 Bad Request")
    public void testRegistrarSignosVitales_TemperaturaInvalida_Retorna400() throws Exception {
        String jsonRequest = """
                {
                    "temperatura": 50.0,
                    "presionArterial": "120/80",
                    "frecuenciaCardiaca": 75,
                    "glucemia": 95.0
                }
                """;

        mockMvc.perform(post("/api/pacientes/1/signos-vitales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.temperatura").exists());
    }

    @Test
    @DisplayName("POST /api/pacientes/{id}/signos-vitales con formato de presión inválido retorna 400 Bad Request")
    public void testRegistrarSignosVitales_PresionFormatoInvalido_Retorna400() throws Exception {
        String jsonRequest = """
                {
                    "temperatura": 36.5,
                    "presionArterial": "120-80",
                    "frecuenciaCardiaca": 75,
                    "glucemia": 95.0
                }
                """;

        mockMvc.perform(post("/api/pacientes/1/signos-vitales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.presionArterial").exists());
    }

    @Test
    @DisplayName("POST /api/pacientes/{id}/signos-vitales cuando el paciente no existe retorna 404 Not Found")
    public void testRegistrarSignosVitales_PacienteInexistente_Retorna404() throws Exception {
        when(signosVitalesService.registrarSignosVitales(eq(99L), any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));

        String jsonRequest = """
                {
                    "temperatura": 36.5,
                    "presionArterial": "120/80",
                    "frecuenciaCardiaca": 75,
                    "glucemia": 95.0
                }
                """;

        mockMvc.perform(post("/api/pacientes/99/signos-vitales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Paciente no encontrado"));
    }

    @Test
    @DisplayName("GET /api/pacientes/{id}/signos-vitales retorna 200 OK con lista ordenada")
    public void testObtenerHistorialSignosVitales_Exito_Retorna200() throws Exception {
        SignosVitalesResponseDTO dto1 = new SignosVitalesResponseDTO(
                2L, 1L, 37.0, "130/80", 80, 105.0, "Dr. Pérez", LocalDateTime.now(), "Tarde"
        );
        SignosVitalesResponseDTO dto2 = new SignosVitalesResponseDTO(
                1L, 1L, 36.5, "120/80", 72, 95.0, "Dr. Pérez", LocalDateTime.now().minusHours(2), "Mañana"
        );

        when(signosVitalesService.obtenerHistorialSignosVitales(1L))
                .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/pacientes/1/signos-vitales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idSignoVital").value(2))
                .andExpect(jsonPath("$[1].idSignoVital").value(1));
    }

    @Test
    @DisplayName("GET /api/pacientes/{id}/signos-vitales cuando paciente no existe retorna 404 Not Found")
    public void testObtenerHistorialSignosVitales_PacienteInexistente_Retorna404() throws Exception {
        when(signosVitalesService.obtenerHistorialSignosVitales(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));

        mockMvc.perform(get("/api/pacientes/99/signos-vitales"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Paciente no encontrado"));
    }
}
