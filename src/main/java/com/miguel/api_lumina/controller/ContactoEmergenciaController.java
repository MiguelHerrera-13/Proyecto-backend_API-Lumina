package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.ContactoEmergenciaRequestDTO;
import com.miguel.api_lumina.dto.ContactoEmergenciaResponseDTO;
import com.miguel.api_lumina.service.ContactoEmergenciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes/{id}/contactos")
public class ContactoEmergenciaController {

    private final ContactoEmergenciaService contactoEmergenciaService;

    public ContactoEmergenciaController(ContactoEmergenciaService contactoEmergenciaService) {
        this.contactoEmergenciaService = contactoEmergenciaService;
    }

    @PostMapping
    public ResponseEntity<ContactoEmergenciaResponseDTO> registrarContacto(
            @PathVariable("id") Long idPaciente,
            @Valid @RequestBody ContactoEmergenciaRequestDTO requestDTO) {
        ContactoEmergenciaResponseDTO response = contactoEmergenciaService.registrarContacto(idPaciente, requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ContactoEmergenciaResponseDTO>> obtenerContactos(
            @PathVariable("id") Long idPaciente) {
        List<ContactoEmergenciaResponseDTO> contactos = contactoEmergenciaService.obtenerContactosPorPaciente(idPaciente);
        return ResponseEntity.ok(contactos);
    }

    @GetMapping("/{idContacto}")
    public ResponseEntity<ContactoEmergenciaResponseDTO> obtenerContactoPorId(
            @PathVariable("id") Long idPaciente,
            @PathVariable("idContacto") Long idContacto) {
        ContactoEmergenciaResponseDTO contacto = contactoEmergenciaService.obtenerContactoPorId(idPaciente, idContacto);
        return ResponseEntity.ok(contacto);
    }

    @PutMapping("/{idContacto}")
    public ResponseEntity<ContactoEmergenciaResponseDTO> actualizarContacto(
            @PathVariable("id") Long idPaciente,
            @PathVariable("idContacto") Long idContacto,
            @Valid @RequestBody ContactoEmergenciaRequestDTO requestDTO) {
        ContactoEmergenciaResponseDTO response = contactoEmergenciaService.actualizarContacto(idPaciente, idContacto, requestDTO);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{idContacto}/principal")
    public ResponseEntity<ContactoEmergenciaResponseDTO> marcarComoPrincipal(
            @PathVariable("id") Long idPaciente,
            @PathVariable("idContacto") Long idContacto) {
        ContactoEmergenciaResponseDTO response = contactoEmergenciaService.marcarComoPrincipal(idPaciente, idContacto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{idContacto}")
    public ResponseEntity<Void> eliminarContacto(
            @PathVariable("id") Long idPaciente,
            @PathVariable("idContacto") Long idContacto) {
        contactoEmergenciaService.eliminarContacto(idPaciente, idContacto);
        return ResponseEntity.noContent().build();
    }
}
