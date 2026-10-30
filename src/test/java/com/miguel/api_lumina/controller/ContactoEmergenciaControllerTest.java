package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.ContactoEmergenciaRequestDTO;
import com.miguel.api_lumina.dto.ContactoEmergenciaResponseDTO;
import com.miguel.api_lumina.exception.GlobalExceptionHandler;
import com.miguel.api_lumina.service.ContactoEmergenciaService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContactoEmergenciaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ContactoEmergenciaService contactoEmergenciaService;

    @InjectMocks
    private ContactoEmergenciaController contactoEmergenciaController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(contactoEmergenciaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/pacientes/1/contactos con datos válidos retorna 201 Created")
    void registrarContacto_Valido_Retorna201() throws Exception {
        String json = "{\"nombre\":\"María Pérez\",\"relacion\":\"Hija\",\"telefono\":\"+5491122334455\",\"esPrincipal\":true}";

        ContactoEmergenciaResponseDTO response = new ContactoEmergenciaResponseDTO(
                1L, 1L, "María Pérez", "Hija", "+5491122334455", true, true
        );

        when(contactoEmergenciaService.registrarContacto(eq(1L), any(ContactoEmergenciaRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/pacientes/1/contactos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idContacto").value(1L))
                .andExpect(jsonPath("$.idPaciente").value(1L))
                .andExpect(jsonPath("$.nombre").value("María Pérez"))
                .andExpect(jsonPath("$.relacion").value("Hija"))
                .andExpect(jsonPath("$.telefono").value("+5491122334455"))
                .andExpect(jsonPath("$.esPrincipal").value(true));
    }

    @Test
    @DisplayName("POST /api/pacientes/1/contactos con nombre vacío retorna 400 Bad Request")
    void registrarContacto_NombreVacio_Retorna400() throws Exception {
        String json = "{\"nombre\":\"\",\"relacion\":\"Hija\",\"telefono\":\"+5491122334455\",\"esPrincipal\":true}";

        mockMvc.perform(post("/api/pacientes/1/contactos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").exists());
    }

    @Test
    @DisplayName("POST /api/pacientes/1/contactos con teléfono inválido retorna 400 Bad Request")
    void registrarContacto_TelefonoInvalido_Retorna400() throws Exception {
        String json = "{\"nombre\":\"María\",\"relacion\":\"Hija\",\"telefono\":\"abc\",\"esPrincipal\":true}";

        mockMvc.perform(post("/api/pacientes/1/contactos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.telefono").exists());
    }

    @Test
    @DisplayName("POST /api/pacientes/99/contactos cuando el paciente no existe retorna 404 Not Found")
    void registrarContacto_PacienteInexistente_Retorna404() throws Exception {
        String json = "{\"nombre\":\"María\",\"relacion\":\"Hija\",\"telefono\":\"+5491122334455\",\"esPrincipal\":true}";

        when(contactoEmergenciaService.registrarContacto(eq(99L), any(ContactoEmergenciaRequestDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: 99"));

        mockMvc.perform(post("/api/pacientes/99/contactos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Paciente no encontrado con ID: 99"));
    }

    @Test
    @DisplayName("GET /api/pacientes/1/contactos retorna lista con 200 OK")
    void obtenerContactos_Retorna200() throws Exception {
        ContactoEmergenciaResponseDTO response = new ContactoEmergenciaResponseDTO(
                1L, 1L, "María Pérez", "Hija", "+5491122334455", true, true
        );

        when(contactoEmergenciaService.obtenerContactosPorPaciente(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/pacientes/1/contactos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idContacto").value(1L))
                .andExpect(jsonPath("$[0].nombre").value("María Pérez"))
                .andExpect(jsonPath("$[0].esPrincipal").value(true));
    }

    @Test
    @DisplayName("GET /api/pacientes/1/contactos/1 retorna contacto con 200 OK")
    void obtenerContactoPorId_Retorna200() throws Exception {
        ContactoEmergenciaResponseDTO response = new ContactoEmergenciaResponseDTO(
                1L, 1L, "María Pérez", "Hija", "+5491122334455", true, true
        );

        when(contactoEmergenciaService.obtenerContactoPorId(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/pacientes/1/contactos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idContacto").value(1L))
                .andExpect(jsonPath("$.nombre").value("María Pérez"));
    }

    @Test
    @DisplayName("PUT /api/pacientes/1/contactos/1 actualiza y retorna 200 OK")
    void actualizarContacto_Retorna200() throws Exception {
        String json = "{\"nombre\":\"María Modificada\",\"relacion\":\"Hija Mayor\",\"telefono\":\"+5491122334455\",\"esPrincipal\":true}";

        ContactoEmergenciaResponseDTO response = new ContactoEmergenciaResponseDTO(
                1L, 1L, "María Modificada", "Hija Mayor", "+5491122334455", true, true
        );

        when(contactoEmergenciaService.actualizarContacto(eq(1L), eq(1L), any(ContactoEmergenciaRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/pacientes/1/contactos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("María Modificada"))
                .andExpect(jsonPath("$.relacion").value("Hija Mayor"));
    }

    @Test
    @DisplayName("PATCH /api/pacientes/1/contactos/2/principal marca como principal y retorna 200 OK")
    void marcarComoPrincipal_Retorna200() throws Exception {
        ContactoEmergenciaResponseDTO response = new ContactoEmergenciaResponseDTO(
                2L, 1L, "Carlos", "Hijo", "+5491199887766", true, true
        );

        when(contactoEmergenciaService.marcarComoPrincipal(1L, 2L)).thenReturn(response);

        mockMvc.perform(patch("/api/pacientes/1/contactos/2/principal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idContacto").value(2L))
                .andExpect(jsonPath("$.esPrincipal").value(true));
    }

    @Test
    @DisplayName("DELETE /api/pacientes/1/contactos/1 retorna 204 No Content")
    void eliminarContacto_Retorna204() throws Exception {
        doNothing().when(contactoEmergenciaService).eliminarContacto(1L, 1L);

        mockMvc.perform(delete("/api/pacientes/1/contactos/1"))
                .andExpect(status().isNoContent());
    }
}
