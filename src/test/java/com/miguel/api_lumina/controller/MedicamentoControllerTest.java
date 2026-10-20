package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.MedicamentoRequestDTO;
import com.miguel.api_lumina.dto.MedicamentoResponseDTO;
import com.miguel.api_lumina.exception.GlobalExceptionHandler;
import com.miguel.api_lumina.service.MedicamentoService;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MedicamentoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MedicamentoService medicamentoService;

    @InjectMocks
    private MedicamentoController medicamentoController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(medicamentoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/medicamentos con datos válidos retorna 201 Created")
    public void crearMedicamento_Valido_Retorna201() throws Exception {
        MedicamentoResponseDTO responseDTO = new MedicamentoResponseDTO(
                1L, 10L, "Ibuprofeno", "400 mg", "Cada 8 horas", true
        );

        when(medicamentoService.crearMedicamento(any(MedicamentoRequestDTO.class))).thenReturn(responseDTO);

        String jsonRequest = "{"
                + "\"idTratamiento\": 10,"
                + "\"nombre\": \"Ibuprofeno\","
                + "\"dosis\": \"400 mg\","
                + "\"frecuencia\": \"Cada 8 horas\""
                + "}";

        mockMvc.perform(post("/api/medicamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMedicamento").value(1L))
                .andExpect(jsonPath("$.idTratamiento").value(10L))
                .andExpect(jsonPath("$.nombre").value("Ibuprofeno"))
                .andExpect(jsonPath("$.dosis").value("400 mg"))
                .andExpect(jsonPath("$.frecuencia").value("Cada 8 horas"))
                .andExpect(jsonPath("$.activo").value(true));

        verify(medicamentoService).crearMedicamento(any(MedicamentoRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/medicamentos con datos inválidos retorna 400 Bad Request")
    public void crearMedicamento_Invalido_Retorna400() throws Exception {
        String jsonRequestInvalido = "{"
                + "\"idTratamiento\": null,"
                + "\"nombre\": \"\","
                + "\"dosis\": \"\","
                + "\"frecuencia\": \"\""
                + "}";

        mockMvc.perform(post("/api/medicamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.idTratamiento").exists())
                .andExpect(jsonPath("$.nombre").exists())
                .andExpect(jsonPath("$.dosis").exists())
                .andExpect(jsonPath("$.frecuencia").exists());

        verify(medicamentoService, never()).crearMedicamento(any());
    }

    @Test
    @DisplayName("POST /api/medicamentos con tratamiento inexistente retorna 404 Not Found")
    public void crearMedicamento_TratamientoInexistente_Retorna404() throws Exception {
        when(medicamentoService.crearMedicamento(any(MedicamentoRequestDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "El tratamiento con ID 999 no existe"));

        String jsonRequest = "{"
                + "\"idTratamiento\": 999,"
                + "\"nombre\": \"Ibuprofeno\","
                + "\"dosis\": \"400 mg\","
                + "\"frecuencia\": \"Cada 8 horas\""
                + "}";

        mockMvc.perform(post("/api/medicamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("El tratamiento con ID 999 no existe"));
    }

    @Test
    @DisplayName("GET /api/medicamentos/{id} con ID existente retorna 200 OK")
    public void obtenerMedicamentoPorId_Existe_Retorna200() throws Exception {
        MedicamentoResponseDTO responseDTO = new MedicamentoResponseDTO(
                1L, 10L, "Paracetamol", "500 mg", "Cada 8 horas", true
        );

        when(medicamentoService.obtenerMedicamentoPorId(1L)).thenReturn(Optional.of(responseDTO));

        mockMvc.perform(get("/api/medicamentos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMedicamento").value(1L))
                .andExpect(jsonPath("$.nombre").value("Paracetamol"));

        verify(medicamentoService).obtenerMedicamentoPorId(1L);
    }

    @Test
    @DisplayName("GET /api/medicamentos/{id} con ID inexistente retorna 404 Not Found")
    public void obtenerMedicamentoPorId_NoExiste_Retorna404() throws Exception {
        when(medicamentoService.obtenerMedicamentoPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/medicamentos/99"))
                .andExpect(status().isNotFound());

        verify(medicamentoService).obtenerMedicamentoPorId(99L);
    }

    @Test
    @DisplayName("GET /api/medicamentos/tratamiento/{idTratamiento} retorna lista y 200 OK")
    public void obtenerMedicamentosPorTratamiento_Retorna200() throws Exception {
        MedicamentoResponseDTO m1 = new MedicamentoResponseDTO(1L, 5L, "Losartan", "50 mg", "Cada 24 horas", true);
        MedicamentoResponseDTO m2 = new MedicamentoResponseDTO(2L, 5L, "Aspirina", "100 mg", "Cada 24 horas", true);

        when(medicamentoService.obtenerMedicamentosPorTratamiento(5L)).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/medicamentos/tratamiento/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Losartan"))
                .andExpect(jsonPath("$[1].nombre").value("Aspirina"));

        verify(medicamentoService).obtenerMedicamentosPorTratamiento(5L);
    }

    @Test
    @DisplayName("PUT /api/medicamentos/{id} con datos válidos retorna 200 OK")
    public void actualizarMedicamento_Valido_Retorna200() throws Exception {
        MedicamentoResponseDTO responseDTO = new MedicamentoResponseDTO(
                1L, 10L, "Ibuprofeno", "600 mg", "Cada 8 horas", true
        );

        when(medicamentoService.actualizarMedicamento(eq(1L), any(MedicamentoRequestDTO.class))).thenReturn(responseDTO);

        String jsonRequest = "{"
                + "\"idTratamiento\": 10,"
                + "\"nombre\": \"Ibuprofeno\","
                + "\"dosis\": \"600 mg\","
                + "\"frecuencia\": \"Cada 8 horas\""
                + "}";

        mockMvc.perform(put("/api/medicamentos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dosis").value("600 mg"));

        verify(medicamentoService).actualizarMedicamento(eq(1L), any(MedicamentoRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/medicamentos/{id} retorna 204 No Content")
    public void darDeBajaMedicamento_Retorna204() throws Exception {
        doNothing().when(medicamentoService).darDeBajaMedicamento(1L);

        mockMvc.perform(delete("/api/medicamentos/1"))
                .andExpect(status().isNoContent());

        verify(medicamentoService).darDeBajaMedicamento(1L);
    }
}
