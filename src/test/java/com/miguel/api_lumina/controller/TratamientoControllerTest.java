package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.TratamientoRequestDTO;
import com.miguel.api_lumina.dto.TratamientoResponseDTO;
import com.miguel.api_lumina.exception.GlobalExceptionHandler;
import com.miguel.api_lumina.service.TratamientoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TratamientoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TratamientoService tratamientoService;

    @InjectMocks
    private TratamientoController tratamientoController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(tratamientoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/tratamientos con datos válidos debe retornar 201 Created")
    public void registrarTratamiento_Valido_Retorna201() throws Exception {
        TratamientoResponseDTO responseDTO = new TratamientoResponseDTO(
                1L, 10L, "Juan Perez", 5L, "Dr. Lopez",
                "Tratamiento de prueba",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 10),
                true
        );

        when(tratamientoService.registrarTratamiento(any(TratamientoRequestDTO.class))).thenReturn(responseDTO);

        String jsonRequest = "{"
                + "\"idPaciente\": 10,"
                + "\"idMedico\": 5,"
                + "\"descripcion\": \"Tratamiento de prueba\","
                + "\"fechaInicio\": \"2026-10-01\","
                + "\"fechaFin\": \"2026-10-10\""
                + "}";

        mockMvc.perform(post("/api/tratamientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTratamiento").value(1L))
                .andExpect(jsonPath("$.idPaciente").value(10L))
                .andExpect(jsonPath("$.nombrePaciente").value("Juan Perez"))
                .andExpect(jsonPath("$.idMedico").value(5L))
                .andExpect(jsonPath("$.nombreMedico").value("Dr. Lopez"))
                .andExpect(jsonPath("$.descripcion").value("Tratamiento de prueba"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("POST /api/tratamientos con datos faltantes debe retornar 400 Bad Request")
    public void registrarTratamiento_Invalido_Retorna400() throws Exception {
        // Enviar JSON sin descripcion ni fechas
        String jsonInvalido = "{"
                + "\"idPaciente\": 10,"
                + "\"idMedico\": 5"
                + "}";

        mockMvc.perform(post("/api/tratamientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.descripcion").exists())
                .andExpect(jsonPath("$.fechaInicio").exists())
                .andExpect(jsonPath("$.fechaFin").exists());
    }

    @Test
    @DisplayName("GET /api/tratamientos/paciente/{id} debe retornar 200 OK con la lista de tratamientos")
    public void obtenerTratamientosPorPaciente_Retorna200() throws Exception {
        TratamientoResponseDTO t1 = new TratamientoResponseDTO(
                1L, 10L, "Juan Perez", 5L, "Dr. Lopez",
                "Tratamiento 1",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 10),
                true
        );

        when(tratamientoService.obtenerTratamientosPorPaciente(eq(10L))).thenReturn(List.of(t1));

        mockMvc.perform(get("/api/tratamientos/paciente/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idTratamiento").value(1L))
                .andExpect(jsonPath("$[0].descripcion").value("Tratamiento 1"));
    }

    @Test
    @DisplayName("GET /api/tratamientos/{id} existente debe retornar 200 OK")
    public void obtenerTratamientoPorId_Existente_Retorna200() throws Exception {
        TratamientoResponseDTO t = new TratamientoResponseDTO(
                1L, 10L, "Juan Perez", 5L, "Dr. Lopez",
                "Tratamiento 1",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 10),
                true
        );

        when(tratamientoService.obtenerTratamientoPorId(eq(1L))).thenReturn(Optional.of(t));

        mockMvc.perform(get("/api/tratamientos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTratamiento").value(1L))
                .andExpect(jsonPath("$.descripcion").value("Tratamiento 1"));
    }

    @Test
    @DisplayName("GET /api/tratamientos/{id} inexistente debe retornar 404 Not Found")
    public void obtenerTratamientoPorId_Inexistente_Retorna404() throws Exception {
        when(tratamientoService.obtenerTratamientoPorId(eq(99L))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tratamientos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/tratamientos/{id} debe retornar 204 No Content")
    public void eliminarTratamiento_Retorna204() throws Exception {
        doNothing().when(tratamientoService).darDeBajaTratamiento(1L);

        mockMvc.perform(delete("/api/tratamientos/1"))
                .andExpect(status().isNoContent());

        verify(tratamientoService, times(1)).darDeBajaTratamiento(1L);
    }
}
