package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.ContactoEmergenciaRequestDTO;
import com.miguel.api_lumina.dto.ContactoEmergenciaResponseDTO;
import com.miguel.api_lumina.entity.ContactoEmergenciaEntity;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.repository.ContactoEmergenciaRepository;
import com.miguel.api_lumina.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
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
class ContactoEmergenciaServiceTest {

    @Mock
    private ContactoEmergenciaRepository contactoEmergenciaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private ContactoEmergenciaService contactoEmergenciaService;

    private PacienteEntity paciente;

    @BeforeEach
    void setUp() {
        paciente = new PacienteEntity();
        paciente.setIdPaciente(1L);
        paciente.setNombreCompleto("Juan Pérez");
    }

    @Test
    @DisplayName("El primer contacto registrado debe marcarse automáticamente como esPrincipal = true")
    void registrarContacto_PrimerContacto_EsPrincipalTrue() {
        ContactoEmergenciaRequestDTO request = new ContactoEmergenciaRequestDTO(
                "María Pérez", "Hija", "+5491122334455", false
        );

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(contactoEmergenciaRepository.existsByPacienteIdPacienteAndTelefono(1L, "+5491122334455")).thenReturn(false);
        when(contactoEmergenciaRepository.countByPacienteIdPaciente(1L)).thenReturn(0L);

        when(contactoEmergenciaRepository.save(any(ContactoEmergenciaEntity.class))).thenAnswer(invocation -> {
            ContactoEmergenciaEntity entity = invocation.getArgument(0);
            entity.setIdContactoEmergencia(10L);
            return entity;
        });

        ContactoEmergenciaResponseDTO response = contactoEmergenciaService.registrarContacto(1L, request);

        assertNotNull(response);
        assertEquals(10L, response.getIdContacto());
        assertEquals("María Pérez", response.getNombre());
        assertTrue(response.getEsPrincipal(), "El primer contacto debe ser principal");
    }

    @Test
    @DisplayName("Registrar un segundo contacto con esPrincipal = false respeta el valor si ya existe un principal")
    void registrarContacto_SegundoContacto_EsPrincipalFalse() {
        ContactoEmergenciaRequestDTO request = new ContactoEmergenciaRequestDTO(
                "Carlos Gómez", "Vecino", "+5491199887766", false
        );

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(contactoEmergenciaRepository.existsByPacienteIdPacienteAndTelefono(1L, "+5491199887766")).thenReturn(false);
        when(contactoEmergenciaRepository.countByPacienteIdPaciente(1L)).thenReturn(1L);
        when(contactoEmergenciaRepository.findFirstByPacienteIdPacienteAndEsPrincipalTrue(1L))
                .thenReturn(Optional.of(new ContactoEmergenciaEntity()));

        when(contactoEmergenciaRepository.save(any(ContactoEmergenciaEntity.class))).thenAnswer(invocation -> {
            ContactoEmergenciaEntity entity = invocation.getArgument(0);
            entity.setIdContactoEmergencia(11L);
            return entity;
        });

        ContactoEmergenciaResponseDTO response = contactoEmergenciaService.registrarContacto(1L, request);

        assertNotNull(response);
        assertFalse(response.getEsPrincipal());
        verify(contactoEmergenciaRepository, never()).resetAllPrincipalesByPacienteId(anyLong());
    }

    @Test
    @DisplayName("Registrar nuevo contacto con esPrincipal = true desmarca a los anteriores asegurando máximo un principal")
    void registrarContacto_NuevoPrincipal_DesmarcaAnteriores() {
        ContactoEmergenciaRequestDTO request = new ContactoEmergenciaRequestDTO(
                "Dra. Laura", "Médica Cabecera", "+5491144556677", true
        );

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(contactoEmergenciaRepository.existsByPacienteIdPacienteAndTelefono(1L, "+5491144556677")).thenReturn(false);
        when(contactoEmergenciaRepository.countByPacienteIdPaciente(1L)).thenReturn(2L);

        when(contactoEmergenciaRepository.save(any(ContactoEmergenciaEntity.class))).thenAnswer(invocation -> {
            ContactoEmergenciaEntity entity = invocation.getArgument(0);
            entity.setIdContactoEmergencia(12L);
            return entity;
        });

        ContactoEmergenciaResponseDTO response = contactoEmergenciaService.registrarContacto(1L, request);

        assertTrue(response.getEsPrincipal());
        verify(contactoEmergenciaRepository).resetAllPrincipalesByPacienteId(1L);
    }

    @Test
    @DisplayName("Registrar contacto con teléfono duplicado lanza 409 CONFLICT")
    void registrarContacto_TelefonoDuplicado_Lanza409() {
        ContactoEmergenciaRequestDTO request = new ContactoEmergenciaRequestDTO(
                "Duplicado", "Hijo", "+5491122334455", false
        );

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(contactoEmergenciaRepository.existsByPacienteIdPacienteAndTelefono(1L, "+5491122334455")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                contactoEmergenciaService.registrarContacto(1L, request)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    @DisplayName("Registrar contacto para paciente inexistente lanza 404 NOT_FOUND")
    void registrarContacto_PacienteInexistente_Lanza404() {
        ContactoEmergenciaRequestDTO request = new ContactoEmergenciaRequestDTO(
                "Test", "Familiar", "+5491122334455", false
        );

        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                contactoEmergenciaService.registrarContacto(99L, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("Obtener contactos por paciente retorna lista ordenada con principal primero")
    void obtenerContactosPorPaciente_RetornaLista() {
        ContactoEmergenciaEntity c1 = new ContactoEmergenciaEntity(paciente, "Principal", "Hija", "111", true);
        c1.setIdContactoEmergencia(1L);
        ContactoEmergenciaEntity c2 = new ContactoEmergenciaEntity(paciente, "Secundario", "Hijo", "222", false);
        c2.setIdContactoEmergencia(2L);

        when(pacienteRepository.existsById(1L)).thenReturn(true);
        when(contactoEmergenciaRepository.findByPacienteIdPacienteOrderByEsPrincipalDescIdContactoEmergenciaAsc(1L))
                .thenReturn(List.of(c1, c2));

        List<ContactoEmergenciaResponseDTO> lista = contactoEmergenciaService.obtenerContactosPorPaciente(1L);

        assertEquals(2, lista.size());
        assertTrue(lista.get(0).getEsPrincipal());
        assertFalse(lista.get(1).getEsPrincipal());
    }

    @Test
    @DisplayName("Marcar contacto como principal resetea anteriores y lo guarda como principal")
    void marcarComoPrincipal_Exito() {
        ContactoEmergenciaEntity c = new ContactoEmergenciaEntity(paciente, "Secundario", "Hijo", "222", false);
        c.setIdContactoEmergencia(2L);

        when(pacienteRepository.existsById(1L)).thenReturn(true);
        when(contactoEmergenciaRepository.findByIdContactoEmergenciaAndPacienteIdPaciente(2L, 1L))
                .thenReturn(Optional.of(c));
        when(contactoEmergenciaRepository.save(any(ContactoEmergenciaEntity.class))).thenAnswer(i -> i.getArgument(0));

        ContactoEmergenciaResponseDTO response = contactoEmergenciaService.marcarComoPrincipal(1L, 2L);

        assertTrue(response.getEsPrincipal());
        verify(contactoEmergenciaRepository).resetAllPrincipalesByPacienteId(1L);
    }

    @Test
    @DisplayName("Eliminar contacto principal promueve automáticamente al siguiente contacto restante")
    void eliminarContacto_Principal_PromueveSiguiente() {
        ContactoEmergenciaEntity cPrincipal = new ContactoEmergenciaEntity(paciente, "Principal", "Hija", "111", true);
        cPrincipal.setIdContactoEmergencia(1L);

        ContactoEmergenciaEntity cSiguiente = new ContactoEmergenciaEntity(paciente, "Siguiente", "Hijo", "222", false);
        cSiguiente.setIdContactoEmergencia(2L);

        when(pacienteRepository.existsById(1L)).thenReturn(true);
        when(contactoEmergenciaRepository.findByIdContactoEmergenciaAndPacienteIdPaciente(1L, 1L))
                .thenReturn(Optional.of(cPrincipal));
        when(contactoEmergenciaRepository.findFirstByPacienteIdPacienteOrderByEsPrincipalDescIdContactoEmergenciaAsc(1L))
                .thenReturn(Optional.of(cSiguiente));

        contactoEmergenciaService.eliminarContacto(1L, 1L);

        verify(contactoEmergenciaRepository).delete(cPrincipal);
        assertTrue(cSiguiente.getEsPrincipal());
        verify(contactoEmergenciaRepository).save(cSiguiente);
    }
}
